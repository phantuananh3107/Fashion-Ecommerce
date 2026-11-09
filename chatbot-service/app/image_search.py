"""
image_search — Tìm kiếm sản phẩm bằng hình ảnh (Giai đoạn 1: CV truyền thống).

Đặc trưng (giải thích được, không cần huấn luyện):
  - Color Histogram: HSV 8x8x8 = 512 bins (màu chủ đạo, phân bố màu)
  - Texture: LBP 8-láng-gềng, 256 bins (kết cấu: ren, dệt thô, nhẵn, sọc)

Vector = concat(0.6 * hist, 0.4 * lbp), L2-normalize. So khớp bằng cosine
similarity quét tuyến tính trên numpy — với ~100 sản phẩm chi phí < 5ms.

Index được build lười (lazy) từ ảnh sản phẩm trong DB, cache tại
data/image_features.npz; tự rebuild khi danh sách (id, đường dẫn, mtime)
thay đổi.
"""
from __future__ import annotations

import hashlib
import threading
import time
from pathlib import Path
from typing import Any

import numpy as np

DATA_DIR = Path(__file__).resolve().parent.parent / "data"
FEATURES_FILE = DATA_DIR / "image_features.npz"

# Thư mục gốc chứa ảnh sản phẩm (đường dẫn trong DB dạng /templates/admin/img/xxx.webp)
_IMAGE_ROOT_CANDIDATES = [
    Path(r"D:\models\apache-tomcat-9.0.123\webapps\ROOT"),  # Tomcat deploy ROOT
    Path(__file__).resolve().parent.parent.parent / "src" / "main" / "webapp",  # repo
]
_env_root = __import__("os").getenv("PRODUCT_IMAGE_ROOT")
if _env_root:
    _IMAGE_ROOT_CANDIDATES.insert(0, Path(_env_root))

MAX_UPLOAD_BYTES = 8 * 1024 * 1024
DEFAULT_TOP_K = 10
W_COLOR = 0.6
W_TEXTURE = 0.4

_lock = threading.Lock()
_index: dict[str, Any] | None = None


class ImageSearchError(RuntimeError):
    """Lỗi khi build index hoặc trích đặc trưng."""


def _resolve_image_root(rel_path: str) -> Path | None:
    rel = rel_path.lstrip("/\\")
    for root in _IMAGE_ROOT_CANDIDATES:
        p = root / rel
        if p.is_file():
            return p
    return None


def _load_product_images() -> list[tuple[int, Path]]:
    """Đọc (id, đường dẫn ảnh chính) từ DB. Dùng chung cấu hình DB của app.tools."""
    from app.tools import _connect

    conn = _connect()
    try:
        with conn.cursor() as cur:
            cur.execute(
                "SELECT id, COALESCE(product_image1, product_image) AS img "
                "FROM product WHERE isDeleted = 0 AND COALESCE(product_image1, product_image) IS NOT NULL"
            )
            rows = cur.fetchall()
    finally:
        conn.close()

    items: list[tuple[int, Path]] = []
    for r in rows:
        p = _resolve_image_root(str(r["img"]))
        if p is not None:
            items.append((int(r["id"]), p))
    return items


def _extract_feature(img_bgr: "np.ndarray") -> np.ndarray:
    """Trả về vector 1-D đã L2-normalize: [0.6*hist_hsv_512, 0.4*lbp_256]."""
    import cv2

    img = cv2.resize(img_bgr, (256, 256), interpolation=cv2.INTER_AREA)
    hsv = cv2.cvtColor(img, cv2.COLOR_BGR2HSV)
    hist = cv2.calcHist([hsv], [0, 1, 2], None, [8, 8, 8], [0, 180, 0, 256, 0, 256])
    hist = hist.flatten().astype(np.float32)
    hist /= hist.sum() + 1e-8

    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    center = gray[1:-1, 1:-1].astype(np.int16)
    code = np.zeros_like(center, dtype=np.uint8)
    offsets = [(-1, -1), (-1, 0), (-1, 1), (0, 1), (1, 1), (1, 0), (1, -1), (0, -1)]
    h, w = center.shape
    for i, (dy, dx) in enumerate(offsets):
        neighbor = gray[1 + dy:1 + dy + h, 1 + dx:1 + dx + w].astype(np.int16)
        code |= ((neighbor >= center).astype(np.uint8) << i)
    lbp = cv2.calcHist([code], [0], None, [256], [0, 256]).flatten().astype(np.float32)
    lbp /= lbp.sum() + 1e-8

    vec = np.concatenate([W_COLOR * hist, W_TEXTURE * lbp])
    norm = float(np.linalg.norm(vec))
    if norm > 0:
        vec /= norm
    return vec


def _manifest_signature(items: list[tuple[int, Path]]) -> str:
    parts = [f"{pid}:{p.stat().st_mtime_ns}" for pid, p in items]
    return hashlib.sha1("|".join(parts).encode()).hexdigest()


def build_index(force: bool = False) -> dict[str, Any]:
    """Build/làm mới index đặc trưng. Trả về meta {n_products, dim, ms}."""
    global _index
    t0 = time.perf_counter()
    items = _load_product_images()
    if not items:
        raise ImageSearchError("Không tìm thấy ảnh sản phẩm nào trong DB / thư mục ảnh.")

    if not force and FEATURES_FILE.exists():
        try:
            data = np.load(FEATURES_FILE, allow_pickle=True)
            if str(data["signature"]) == _manifest_signature(items):
                _index = {
                    "ids": data["ids"],
                    "matrix": data["matrix"],
                }
                return {
                    "n_products": int(_index["ids"].size),
                    "dim": int(_index["matrix"].shape[1]),
                    "ms": round((time.perf_counter() - t0) * 1000, 1),
                    "cached": True,
                }
        except Exception:  # noqa: BLE001 — cache hỏng thì build lại
            pass

    import cv2

    ids: list[int] = []
    vecs: list[np.ndarray] = []
    for pid, path in items:
        img = cv2.imdecode(np.fromfile(str(path), dtype=np.uint8), cv2.IMREAD_COLOR)
        if img is None:
            continue
        vecs.append(_extract_feature(img))
        ids.append(pid)

    if not vecs:
        raise ImageSearchError("Không decode được ảnh sản phẩm nào (định dạng webp/jpg?).")

    matrix = np.vstack(vecs).astype(np.float32)
    ids_arr = np.array(ids, dtype=np.int64)
    signature = _manifest_signature(items)
    np.savez_compressed(FEATURES_FILE, ids=ids_arr, matrix=matrix, signature=signature)
    _index = {"ids": ids_arr, "matrix": matrix}
    return {
        "n_products": len(ids),
        "dim": int(matrix.shape[1]),
        "ms": round((time.perf_counter() - t0) * 1000, 1),
        "cached": False,
    }


def _ensure_index() -> dict[str, Any]:
    global _index
    if _index is not None:
        return _index
    with _lock:
        if _index is None:
            build_index()
    return _index  # type: ignore[return-value]


def extract_feature_from_bytes(image_bytes: bytes) -> np.ndarray:
    import cv2

    arr = np.frombuffer(image_bytes, dtype=np.uint8)
    img = cv2.imdecode(arr, cv2.IMREAD_COLOR)
    if img is None:
        raise ImageSearchError("File không phải ảnh hợp lệ (jpg/png/webp).")
    return _extract_feature(img)


def search(image_bytes: bytes, top_k: int = DEFAULT_TOP_K) -> list[dict[str, Any]]:
    """Trả về top_k [{product_id, score}] theo cosine similarity."""
    idx = _ensure_index()
    q = extract_feature_from_bytes(image_bytes)
    sims = idx["matrix"] @ q
    k = min(top_k, sims.shape[0])
    top = np.argsort(-sims)[:k]
    return [
        {"product_id": int(idx["ids"][i]), "score": round(float(sims[i]), 4)}
        for i in top
    ]


def index_status() -> dict[str, Any]:
    """Trạng thái index cho /health — không ném lỗi."""
    try:
        idx = _ensure_index()
        return {"ok": True, "n_products": int(idx["ids"].size)}
    except Exception as e:  # noqa: BLE001
        return {"ok": False, "error": str(e)}


if __name__ == "__main__":
    meta = build_index()
    print(f"index: {meta['n_products']} sản phẩm, dim={meta['dim']}, {meta['ms']}ms (cached={meta['cached']})")
    # Sanity: query bằng chính ảnh sản phẩm đầu tiên → top-1 phải là chính nó
    items = _load_product_images()
    pid, path = items[0]
    res = search(path.read_bytes(), top_k=3)
    print(f"sanity: query ảnh của product {pid} → top1 = {res[0]['product_id']} (score={res[0]['score']})")
