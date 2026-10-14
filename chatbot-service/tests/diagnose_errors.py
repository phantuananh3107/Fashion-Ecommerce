# -*- coding: utf-8 -*-
"""So sánh Rule vs JEV từng case trên tập test — dùng để phân tích những case bị sai."""
import json
import os
import sys
from pathlib import Path

os.environ.setdefault("HF_HOME", r"D:\models\huggingface")
os.environ.setdefault("TRANSFORMERS_CACHE", r"D:\models\huggingface")
os.environ.setdefault("HUGGINGFACE_HUB_CACHE", r"D:\models\huggingface")

BASE = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(BASE))

from app.intent_matcher import IntentMatcher  # noqa: E402
from app.jev_service import JEVMatcher  # noqa: E402

rule = IntentMatcher()
jev = JEVMatcher()
jev.load()

only_jev_right = 0
only_rule_right = 0
both_wrong = 0
rows = []

with (BASE / "data" / "eval_cases.jsonl").open(encoding="utf-8") as f:
    for line in f:
        c = json.loads(line)
        if c["split"] != "test":
            continue
        t, truth = c["text"], c["intent"]
        r = rule.match(t)
        j = jev.match(t)
        ok_r = r["intent"] == truth
        ok_j = j["intent"] == truth
        if ok_j and not ok_r:
            only_jev_right += 1
        if ok_r and not ok_j:
            only_rule_right += 1
        if not ok_r and not ok_j:
            both_wrong += 1
        rows.append((ok_r, ok_j, r["intent"], r["confidence"], j["intent"], j["confidence"], truth, t))

print(f"JEV đúng & Rule sai : {only_jev_right}")
print(f"Rule đúng & JEV sai: {only_rule_right}")
print(f"Cả hai sai         : {both_wrong}")
print(f"Trùng nhau          : {len(rows) - only_jev_right - only_rule_right - both_wrong}")
print()
print("--- Case JEV đúng, Rule sai (JEV có giá trị thêm) ---")
for ok_r, ok_j, ri, rc, ji, jc, truth, t in rows:
    if ok_j and not ok_r:
        print(f"  text={t[:45]!r}\n    truth={truth}  Rule={ri}({rc:.2f})  JEV={ji}({jc:.2f})")
print()
print("--- Case Rule đúng, JEV sai ---")
for ok_r, ok_j, ri, rc, ji, jc, truth, t in rows:
    if ok_r and not ok_j:
        print(f"  text={t[:45]!r}\n    truth={truth}  Rule={ri}({rc:.2f})  JEV={ji}({jc:.2f})")
print()
print("--- Case cả hai sai ---")
for ok_r, ok_j, ri, rc, ji, jc, truth, t in rows:
    if not ok_r and not ok_j:
        print(f"  text={t[:45]!r}\n    truth={truth}  Rule={ri}({rc:.2f})  JEV={ji}({jc:.2f})")