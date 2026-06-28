#!/usr/bin/env python3
"""
Download CC0 sound effects from Kenney.nl for AnimaQuiz.

Run from the repo root:
    python scripts/download_sounds.py

The script fetches four sounds from the Kenney "Interface Sounds" pack (CC0 1.0)
and places them in app/src/main/res/raw/ where SoundResources.kt expects them.

Kenney Interface Sounds pack: https://kenney.nl/assets/interface-sounds
License: Creative Commons Zero (CC0 1.0) — free for commercial use, no attribution required.
"""

import urllib.request
import os
import sys

OUTPUT_DIR = os.path.join(
    os.path.dirname(__file__), "..", "app", "src", "main", "res", "raw"
)

# Direct URLs to individual OGG files from the Kenney Interface Sounds pack.
# These are stable CDN links; if they break visit kenney.nl to get the updated pack URL.
SOUNDS = {
    "correct.ogg":   "https://kenney.nl/content/interface-sounds/correct.ogg",
    "incorrect.ogg": "https://kenney.nl/content/interface-sounds/error.ogg",
    "complete.ogg":  "https://kenney.nl/content/interface-sounds/confirmation_001.ogg",
    "click.ogg":     "https://kenney.nl/content/interface-sounds/click_002.ogg",
}


def main() -> None:
    os.makedirs(OUTPUT_DIR, exist_ok=True)
    ok, skipped, failed = 0, 0, 0

    for filename, url in SOUNDS.items():
        dest = os.path.join(OUTPUT_DIR, filename)
        if os.path.exists(dest):
            print(f"  skip  {filename} (already exists)")
            skipped += 1
            continue
        print(f"  fetch {filename} …", end=" ", flush=True)
        try:
            urllib.request.urlretrieve(url, dest)
            size = os.path.getsize(dest)
            print(f"OK ({size} bytes)")
            ok += 1
        except Exception as exc:  # noqa: BLE001
            print(f"FAILED: {exc}")
            failed += 1

    print(f"\nDone: {ok} downloaded, {skipped} skipped, {failed} failed.")
    if failed:
        print("\nFailed downloads: manually place the missing OGG files in:")
        print(f"  {os.path.abspath(OUTPUT_DIR)}")
        print("Source: https://kenney.nl/assets/interface-sounds (CC0 1.0)")
        sys.exit(1)


if __name__ == "__main__":
    main()
