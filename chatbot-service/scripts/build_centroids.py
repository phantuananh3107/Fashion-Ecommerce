# -*- coding: utf-8 -*-
import json
import sys
from collections import defaultdict
from pathlib import Path

BASE = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(BASE))

from app.jev_service import MODEL_NAME, QUERY_PREFIX, JEVMatcher  # noqa: E402

EVAL_FILE = BASE / "data" / "eval_cases.jsonl"
OUT_FILE = BASE / "data" / "centroids.json"


def load_train_cases() -> list[dict]:
    cases = []
    with EVAL_FILE.open(encoding="utf-8") as f:
        for line in f:
            r = json.loads(line)
            if r["split"] == "train":
                cases.append(r)
    return cases


def main() -> None:
    cases = load_train_cases()
    if not cases:
        raise SystemExit("Không có case nào với split=train trong eval_cases.jsonl")

    try:
        from sentence_transformers import SentenceTransformer
    except ImportError as e:
        raise SystemExit(
            "Thiếu sentence-transformers. Cài bằng: pip install sentence-transformers torch"
        ) from e

    print(f"Đang tải model {MODEL_NAME} (lần đầu ~1-2 phút)...")
    model = SentenceTransformer(MODEL_NAME)

    by_intent: dict[str, list[str]] = defaultdict(list)
    for c in cases:
        by_intent[c["intent"]].append(c["text"])

    centroids: dict[str, list[float]] = {}
    samples: dict[str, list[list[float]]] = {}
    counts: dict[str, int] = {}
    for intent, texts in sorted(by_intent.items()):
        vecs = model.encode([QUERY_PREFIX + t for t in texts], normalize_embeddings=True)
        import numpy as np

        samples[intent] = [[float(x) for x in v] for v in vecs]
        mean = np.mean(vecs, axis=0)
        norm = np.linalg.norm(mean)
        if norm > 0:
            mean = mean / norm
        centroids[intent] = [float(x) for x in mean]
        counts[intent] = len(texts)
        print(f"  {intent:22s} {len(texts):3d} câu")

    dim = len(next(iter(centroids.values())))
    payload = {
        "meta": {
            "model": MODEL_NAME,
            "dim": dim,
            "n_intents": len(centroids),
            "counts": counts,
            "query_prefix": QUERY_PREFIX,
        },
        "centroids": centroids,
        "samples": samples,
    }
    OUT_FILE.write_text(json.dumps(payload, ensure_ascii=False), encoding="utf-8")
    print(f"\nĐã ghi {len(centroids)} centroid (dim={dim}) → {OUT_FILE}")


if __name__ == "__main__":
    main()
