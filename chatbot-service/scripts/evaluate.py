# -*- coding: utf-8 -*-
"""
Đánh giá 3 phương án: Rule (T1) vs JEV (T2) vs Hybrid (T3) trên eval_cases.jsonl.

Đo từng phương án:
  - accuracy (macro, trên tập đang đánh giá)
  - số lần route theo từng tầng (tier1/tier2/tier3)
  - latency (nếu có model)

JEV thiếu centroids hoặc thiếu lib → phương án JEV/Hybrid tự bỏ qua T2 và báo "SKIP".

Chạy:  python scripts/evaluate.py [--split test] [--pretty]

Kết quả in bảng để dán vào Chương 4 báo cáo.
"""
import argparse
import json
import sys
import time
from collections import Counter
from pathlib import Path

BASE = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(BASE))

from app.intent_matcher import IntentMatcher  # noqa: E402
from app.jev_service import JEVMatcher, JEVNotAvailableError  # noqa: E402
from app.router import HybridRouter  # noqa: E402

EVAL_FILE = BASE / "data" / "eval_cases.jsonl"
CENTROIDS_FILE = BASE / "data" / "centroids.json"


def load_cases(split: str | None) -> list[dict]:
    cases: list[dict] = []
    with EVAL_FILE.open(encoding="utf-8") as f:
        for line in f:
            r = json.loads(line)
            if split is None or r["split"] == split:
                cases.append(r)
    return cases


def evaluate_rule(cases: list[dict]) -> tuple[float, Counter]:
    m = IntentMatcher()
    correct = sum(1 for c in cases if m.match(c["text"])["intent"] == c["intent"])
    return (correct / len(cases) if cases else 0.0), Counter()


def evaluate_jev(cases: list[dict]) -> tuple[float | None, str]:
    try:
        jev = JEVMatcher()
        jev.load()
    except (JEVNotAvailableError, FileNotFoundError, ValueError) as e:
        return None, f"SKIP (JEV: {e})"
    correct = sum(1 for c in cases if jev.match(c["text"])["intent"] == c["intent"])
    return (correct / len(cases) if cases else 0.0), ""


def evaluate_hybrid(cases: list[dict]) -> tuple[float, Counter]:
    jev = None
    try:
        j = JEVMatcher()
        j.load()
        jev = j
    except (JEVNotAvailableError, FileNotFoundError, ValueError):
        jev = None
    router = HybridRouter(jev=jev)
    router.enable_jev = jev is not None
    correct = 0
    routes: Counter = Counter()
    for c in cases:
        r = router.route(c["text"])
        routes[r["route"]] += 1
        if r["intent"] == c["intent"]:
            correct += 1
    return (correct / len(cases) if cases else 0.0), routes


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("--split", choices=["train", "test", "all"], default="test")
    ap.add_argument("--pretty", action="store_true", help="In bảng markdown")
    args = ap.parse_args()

    split: str | None = None if args.split == "all" else args.split
    cases = load_cases(split)
    print(f"Tập đánh giá: split={args.split!r} — {len(cases)} câu")

    # Rule
    t0 = time.perf_counter()
    rule_acc, _ = evaluate_rule(cases)
    t_rule = (time.perf_counter() - t0) * 1000.0 / max(len(cases), 1)
    print(f"Rule   : accuracy={rule_acc:.1%}  avg {t_rule:.1f} ms/query")

    # JEV
    t0 = time.perf_counter()
    jev_res, jev_msg = evaluate_jev(cases)
    if jev_res is None:
        print(f"JEV    : {jev_msg}")
    else:
        t_jev = (time.perf_counter() - t0) * 1000.0 / max(len(cases), 1)
        print(f"JEV    : accuracy={jev_res:.1%}  avg {t_jev:.1f} ms/query")

    # Hybrid
    t0 = time.perf_counter()
    hybrid_acc, routes = evaluate_hybrid(cases)
    t_hy = (time.perf_counter() - t0) * 1000.0 / max(len(cases), 1)
    print(f"Hybrid : accuracy={hybrid_acc:.1%}  avg {t_hy:.1f} ms/query  "
          f"route={dict(routes)}")

    if args.pretty:
        jev_str = f"{jev_res:.1%}" if jev_res is not None else "—"
        print("\n| Phương án | Accuracy | p50 | Route |")
        print("|---|---|---|---|")
        print(f"| T1 Rule | {rule_acc:.1%} | {t_rule:.0f}ms | tier1 |")
        print(f"| T2 JEV | {jev_str} | — | tier2 |")
        print(f"| T3 Hybrid | {hybrid_acc:.1%} | {t_hy:.0f}ms | {dict(routes)} |")


if __name__ == "__main__":
    main()
