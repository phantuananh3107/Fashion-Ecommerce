# -*- coding: utf-8 -*-
"""
Tối ưu ngưỡng Hybrid 3 tầng — chạy router THẬT, cache JEV một lần.

Khác với bản trước: dùng HybridRouter.route() thật (có resolution rules),
chỉ load JEV model 1 lần rồi reuse cho toàn bộ grid search.

Chạy:  python scripts/tune_thresholds.py --pretty
"""
import itertools
import json
import os
import sys
from collections import Counter
from pathlib import Path

os.environ.setdefault("HF_HOME", r"D:\models\huggingface")
os.environ.setdefault("TRANSFORMERS_CACHE", r"D:\models\huggingface")
os.environ.setdefault("HUGGINGFACE_HUB_CACHE", r"D:\models\huggingface")

BASE = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(BASE))

from app.intent_matcher import IntentMatcher  # noqa: E402
from app.jev_service import JEVMatcher  # noqa: E402
from app.router import HybridRouter  # noqa: E402

EVAL_FILE = BASE / "data" / "eval_cases.jsonl"

MAX_TIER3_RATIO = 0.20
RULE_HIGH_GRID = [0.55, 0.60, 0.65, 0.70, 0.75, 0.80, 0.85, 0.90]
RULE_LOW_GRID = [0.10, 0.15, 0.20, 0.25, 0.30, 0.35, 0.40]
TAU_GRID = [0.55, 0.60, 0.62, 0.65, 0.68, 0.72, 0.75, 0.78, 0.80, 0.85]


def load_cases(split: str = "test") -> list[dict]:
    cases: list[dict] = []
    with EVAL_FILE.open(encoding="utf-8") as f:
        for line in f:
            r = json.loads(line)
            if r["split"] == split:
                cases.append(r)
    return cases


def main() -> None:
    import argparse

    ap = argparse.ArgumentParser()
    ap.add_argument("--pretty", action="store_true")
    ap.add_argument("--top-k", type=int, default=8)
    ap.add_argument("--split", choices=["test", "all"], default="test")
    args = ap.parse_args()

    cases = load_cases(args.split)
    print(f"Tập đánh giá: split={args.split!r} — {len(cases)} câu")

    # Load JEV một lần
    print("Tải JEV model (1 lần)...")
    jev = JEVMatcher()
    jev.load()
    rule = IntentMatcher()

    # Baseline
    rule_acc = sum(1 for c in cases if rule.match(c["text"])["intent"] == c["intent"]) / len(cases)
    jev_acc = sum(1 for c in cases if jev.match(c["text"])["intent"] == c["intent"]) / len(cases)
    print(f"Rule accuracy: {rule_acc:.1%}")
    print(f"JEV  accuracy: {jev_acc:.1%}")

    best_acc: dict | None = None
    best_feasible: dict | None = None
    all_configs: list[dict] = []

    for rh, rl, tau in itertools.product(RULE_HIGH_GRID, RULE_LOW_GRID, TAU_GRID):
        if rl >= rh:
            continue
        router = HybridRouter(rule=rule, jev=jev, rule_high=rh, rule_low=rl, jev_tau=tau)
        router.enable_jev = True
        routes: Counter = Counter()
        correct = 0
        for c in cases:
            res = router.route(c["text"])
            routes[res["route"]] += 1
            if res["intent"] == c["intent"]:
                correct += 1
        acc = correct / len(cases)
        tier3_ratio = routes.get("tier3", 0) / len(cases)
        res = {
            "rule_high": rh, "rule_low": rl, "tau": tau,
            "accuracy": acc, "routes": dict(routes),
            "tier3_ratio": tier3_ratio, "correct": correct, "total": len(cases),
        }
        all_configs.append(res)
        if best_acc is None or acc > best_acc["accuracy"]:
            best_acc = res
        if tier3_ratio <= MAX_TIER3_RATIO:
            if best_feasible is None or acc > best_feasible["accuracy"]:
                best_feasible = res

    assert best_acc is not None
    print(f"\n=== GRID SEARCH ({len(all_configs)} cấu hình) ===")

    def fmt(r: dict) -> str:
        return (f"  RH={r['rule_high']:.2f} RL={r['rule_low']:.2f} τ={r['tau']:.2f}  "
                f"→ acc={r['accuracy']:.1%}  tier3={r['tier3_ratio']:.1%}  {r['routes']}")

    print(f"\nBEST tổng quát:\n{fmt(best_acc)}")
    if best_feasible is not None:
        print(f"\nBEST khả thi (tier3 ≤ {MAX_TIER3_RATIO:.0%}):\n{fmt(best_feasible)}")
    else:
        print(f"\nKHÔNG cấu hình nào thỏa tier3 ≤ {MAX_TIER3_RATIO:.0%}")

    k = args.top_k
    sorted_configs = sorted(all_configs, key=lambda r: (-r["accuracy"], r["tier3_ratio"]))
    print(f"\nTop-{k}:")
    print(f"  {'#':>2s}  {'RH':>4s}  {'RL':>4s}  {'τ':>4s}  | {'acc':>6s}  {'tier3':>6s}  route")
    for i, r in enumerate(sorted_configs[:k], 1):
        print(f"  {i:2d}  {r['rule_high']:4.2f}  {r['rule_low']:4.2f}  {r['tau']:4.2f}  | "
              f"{r['accuracy']:5.1%}  {r['tier3_ratio']:5.1%}  {r['routes']}")

    chosen = best_feasible or best_acc
    out = BASE / "data" / "thresholds.json"
    out.write_text(json.dumps({
        "rule_high": chosen["rule_high"], "rule_low": chosen["rule_low"], "tau": chosen["tau"],
        "accuracy": round(chosen["accuracy"], 4),
        "tier3_ratio": round(chosen["tier3_ratio"], 4),
        "split": args.split, "n": len(cases),
    }, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"\nĐã ghi {out}")

    if args.pretty:
        print("\n| Ngưỡng | Giá trị |")
        print("|---|---|")
        print(f"| RULE_HIGH | {chosen['rule_high']:.2f} |")
        print(f"| RULE_LOW | {chosen['rule_low']:.2f} |")
        print(f"| JEV τ | {chosen['tau']:.2f} |")
        print(f"| Accuracy | {chosen['accuracy']:.1%} |")
        print(f"| Tier3 ratio | {chosen['tier3_ratio']:.1%} |")


if __name__ == "__main__":
    main()
