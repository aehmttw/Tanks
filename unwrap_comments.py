#!/usr/bin/env python3
"""
Collapse 3-line block/Javadoc comments into a single line.

Before:
    /**
     * Some comment text
     */

After:
    /** Some comment text */

Also handles plain block comments:
    /*
     * Some comment text
     */
    →  /* Some comment text */

Only collapses when:
  - The opening line has nothing after /* or /** (besides optional whitespace)
  - The middle line has exactly one * followed by the text (no additional * lines)
  - The closing */ has nothing else on its line

Usage:
    python3 unwrap_comments.py [--dry-run] [path ...]

    path: one or more files or directories to process (default: src/)
"""

import re
import sys
from pathlib import Path

# Matches a 3-line block or Javadoc comment with consistent indentation:
#   <indent>/* or /**
#   <indent> * text
#   <indent> */
PATTERN = re.compile(
    r'^([ \t]*)(/\*\*?)[ \t]*\n'  # opening /* or /**
    r'\1 \*[ \t]+(.*?)\n'          # middle:  <indent> * <text>
    r'\1 \*/',                     # closing: <indent> */
    re.MULTILINE,
)


def collapse(text):
    def replacement(m):
        indent = m.group(1)
        opener = m.group(2)   # /* or /**
        content = m.group(3).rstrip()
        return f"{indent}{opener} {content} */"
    return PATTERN.sub(replacement, text)


def process_file(path: Path, dry_run: bool) -> int:
    original = path.read_text(encoding="utf-8")
    updated = collapse(original)
    if updated == original:
        return 0
    count = len(PATTERN.findall(original))
    print(f"{'[dry-run] ' if dry_run else ''}{path}: {count} comment(s)")
    if not dry_run:
        path.write_text(updated, encoding="utf-8")
    return count


def main():
    args = sys.argv[1:]
    dry_run = "--dry-run" in args
    args = [a for a in args if a != "--dry-run"]

    roots = [Path(a) for a in args] if args else [Path("src")]

    files = []
    for root in roots:
        if root.is_file():
            files.append(root)
        else:
            files.extend(root.rglob("*.java"))

    total = sum(process_file(f, dry_run) for f in sorted(files))
    print(f"\n{'Would collapse' if dry_run else 'Collapsed'} {total} comment(s) across the matched files.")


if __name__ == "__main__":
    main()
