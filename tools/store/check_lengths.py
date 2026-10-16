"""Checks store listing texts against Google Play and App Store character limits.

usage: python3 tools/store/check_lengths.py
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parents[2] / "store-assets" / "metinler"
LIMITS = {
    "google-play.md": {"short": 80, "full": 4000},
    "app-store.md": {"subtitle": 30, "promo": 170, "keywords": 100, "description": 4000, "review": 4000},
}
NAME_LIMIT = 30

ok = True
for file, blocks in LIMITS.items():
    text = (ROOT / file).read_text(encoding="utf-8")
    name = re.search(r"\| (?:Uygulama adı|Ad) \(en fazla 30\) \| (.+?) \|", text).group(1)
    print(f"{file}: name {len(name)}/{NAME_LIMIT}")
    ok &= len(name) <= NAME_LIMIT
    for key, limit in blocks.items():
        body = re.search(rf"<!-- {key} -->\n(.*?)\n<!-- /{key} -->", text, re.S).group(1)
        n = len(body)
        flag = "OK" if n <= limit else "TOO LONG"
        print(f"  {key}: {n}/{limit} {flag}")
        ok &= n <= limit
        if key == "keywords" and ", " in body:
            print("  keywords: remove spaces after commas")
            ok = False
sys.exit(0 if ok else 1)
