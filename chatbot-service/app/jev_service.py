import json
import time
from pathlib import Path
from typing import Any

DATA_DIR = Path(__file__).resolve().parent.parent / "data"
CENTROIDS_FILE = DATA_DIR / "centroids.json"

MODEL_NAME = "intfloat/multilingual-e5-small"
QUERY_PREFIX = "query: "
DEFAULT_TAU = 0.72
DEFAULT_MATCH_MODE = "centroid"  # "centroid" | "maxsim"


class JEVNotAvailableError(RuntimeError):
    """Ném khi chưa cài sentence-transformers hoặc torch."""


class JEVMatcher:
    def __init__(
        self,
        model_name: str = MODEL_NAME,
        centroids_file: Path = CENTROIDS_FILE,
        tau: float = DEFAULT_TAU,
        match_mode: str = DEFAULT_MATCH_MODE,
    ):
        self.model_name = model_name
        self.centroids_file = centroids_file
        self.tau = tau
        self.match_mode = match_mode
        self._model = None
        self._centroids: dict[str, list[float]] = {}
        self._intent_names: list[str] = []
        self._matrix = None  # np.ndarray (n_intent, dim)
        self.model_meta: dict[str, Any] = {}

    def load(self) -> "JEVMatcher":
        """Tải model embedding + centroids. Ném JEVNotAvailableError nếu thiếu lib."""
        try:
            from sentence_transformers import SentenceTransformer
        except ImportError as e:
            raise JEVNotAvailableError(
                "Chưa cài sentence-transformers. Chạy: pip install sentence-transformers torch"
            ) from e
        try:
            import numpy as np  # noqa
        except ImportError as e:
            raise JEVNotAvailableError("Chưa cài numpy. Chạy: pip install numpy") from e

        import numpy as _np

        self._model = SentenceTransformer(self.model_name)

        data = json.loads(self.centroids_file.read_text(encoding="utf-8"))
        meta = data.get("meta", {})
        if meta.get("model") != self.model_name:
            raise ValueError(
                f"centroids.json dùng model {meta.get('model')!r}, "
                f"nhưng JEVMatcher yêu cầu {self.model_name!r}. "
                "Chạy lại scripts/build_centroids.py."
            )
        self.model_meta = meta
        self._centroids = data["centroids"]
        self._intent_names = sorted(self._centroids.keys())
        self._matrix = _np.array([self._centroids[n] for n in self._intent_names], dtype=_np.float32)
        # Nếu centroids.json chứa cả mẫu câu gốc → dùng max-similarity thay vì centroid trung bình
        self._samples: dict[str, list[list[float]]] = {}
        if "samples" in data:
            self._samples = {
                k: [_np.asarray(v, dtype=_np.float32) for v in vs]
                for k, vs in data["samples"].items()
            }
        return self

    @property
    def loaded(self) -> bool:
        return self._model is not None and self._matrix is not None

    def embed(self, text: str) -> list[float]:
        """Vector hóa 1 câu (L2-normalized)."""
        if not self.loaded:
            raise JEVNotAvailableError("Gọi jev.load() trước khi embed.")
        vec = self._model.encode(QUERY_PREFIX + text, normalize_embeddings=True)
        return [float(x) for x in vec]

    def match(self, text: str) -> dict[str, Any]:
        """
        Trả về:
        {
            "intent": str, "confidence": float (0..1),
            "top3": [(intent, conf)], "latency_ms": float, "above_tau": bool
        }
        confidence = (cosine + 1) / 2
        """
        import numpy as _np

        if not self.loaded:
            raise JEVNotAvailableError("Gọi jev.load() trước khi match.")

        t0 = time.perf_counter()
        q = _np.array(
            self._model.encode(QUERY_PREFIX + text, normalize_embeddings=True),
            dtype=_np.float32,
        )

        if self.match_mode == "maxsim" and self._samples:
            scored = []
            for intent, vecs in self._samples.items():
                best_sim = max(float(_np.dot(v, q)) for v in vecs) if vecs else -1.0
                scored.append((best_sim, intent))
            scored.sort(reverse=True)
        else:
            sims = self._matrix @ q
            scored = sorted(
                ((float(sims[i]), self._intent_names[i]) for i in range(len(sims))),
                reverse=True,
            )

        elapsed = (time.perf_counter() - t0) * 1000.0

        top3 = [(intent, round((sim + 1.0) / 2.0, 3)) for sim, intent in scored[:3]]
        _, best_intent = scored[0]
        best_conf = top3[0][1]

        return {
            "intent": best_intent,
            "confidence": best_conf,
            "top3": top3,
            "latency_ms": round(elapsed, 1),
            "above_tau": best_conf >= self.tau,
        }


if __name__ == "__main__":
    jev = JEVMatcher()
    try:
        jev.load()
    except JEVNotAvailableError as e:
        print(f"SKIP: {e}")
        raise SystemExit(0)
    for q in [
        "tim ao thun den size l",
        "don DH1234 dang o dau",
        "thoi tiet hom nay the nao",
        "minh di bien cuoi tuan mac gi dep",
    ]:
        r = jev.match(q)
        print(f"[{r['intent']:18s}] conf={r['confidence']:.2f} ({r['latency_ms']}ms) | {q}")
