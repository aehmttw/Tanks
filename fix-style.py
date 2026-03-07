#!/usr/bin/env python3
"""
Automatic Java source-code fixer.

Reads the indent size from config/checkstyle/checkstyle.xml, then applies
the following fixes to every .java file under src/:

  - Tabs → spaces  (expandtabs with the configured indent size)
  - Trailing whitespace removed from every line
  - File ends with exactly one newline
  - Space inserted after commas  where missing
  - Space inserted after semicolons  where missing (e.g. for-loop headers)

String literals, char literals, line comments (//) and block comments
(/* … */) are all skipped by the whitespace fixers to avoid false edits.
"""

import xml.etree.ElementTree as ET
from pathlib import Path


# ---------------------------------------------------------------------------
# Read config
# ---------------------------------------------------------------------------

def read_indent_size(config_path: Path, default: int = 4) -> int:
    """Return basicOffset from the Indentation module in checkstyle.xml."""
    try:
        root = ET.parse(config_path).getroot()
        for module in root.iter("module"):
            if module.get("name") == "Indentation":
                for prop in module.findall("property"):
                    if prop.get("name") == "basicOffset":
                        return int(prop.get("value", default))
    except Exception:
        pass
    return default


# ---------------------------------------------------------------------------
# Literal / comment masking
# ---------------------------------------------------------------------------

def mask_literals(text: str) -> str:
    """
    Return a copy of *text* with the same length where every character
    inside a string literal, char literal, line comment, or block comment
    is replaced with a space.  Newlines are always preserved so that line
    numbers stay consistent with the original.

    This lets whitespace fixers work on the masked copy to find positions
    of interest, then apply changes to the original text.
    """
    buf = list(text)
    i = 0
    n = len(text)

    while i < n:
        c = text[i]

        # Line comment  //
        if c == '/' and i + 1 < n and text[i + 1] == '/':
            while i < n and text[i] != '\n':
                buf[i] = ' '
                i += 1

        # Block comment  /* … */
        elif c == '/' and i + 1 < n and text[i + 1] == '*':
            buf[i] = buf[i + 1] = ' '
            i += 2
            while i < n:
                if text[i] == '*' and i + 1 < n and text[i + 1] == '/':
                    buf[i] = buf[i + 1] = ' '
                    i += 2
                    break
                if text[i] != '\n':
                    buf[i] = ' '
                i += 1

        # String literal  "…"
        elif c == '"':
            buf[i] = ' '
            i += 1
            while i < n and text[i] != '\n':
                if text[i] == '\\':        # escaped character
                    buf[i] = ' '
                    i += 1
                    if i < n:
                        buf[i] = ' '
                        i += 1
                elif text[i] == '"':
                    buf[i] = ' '
                    i += 1
                    break
                else:
                    buf[i] = ' '
                    i += 1

        # Char literal  '…'
        elif c == "'":
            buf[i] = ' '
            i += 1
            while i < n and text[i] != '\n':
                if text[i] == '\\':
                    buf[i] = ' '
                    i += 1
                    if i < n:
                        buf[i] = ' '
                        i += 1
                elif text[i] == "'":
                    buf[i] = ' '
                    i += 1
                    break
                else:
                    buf[i] = ' '
                    i += 1

        else:
            i += 1

    return ''.join(buf)


# ---------------------------------------------------------------------------
# Individual fixers
# ---------------------------------------------------------------------------

def fix_line_endings(text: str) -> str:
    """Normalise all line endings to Unix LF (\\n)."""
    # \r\n first so the \r isn't converted separately by the second replace
    return text.replace('\r\n', '\n').replace('\r', '\n')


def fix_tabs(text: str, indent: int) -> str:
    """Expand every tab to the next indent-sized column boundary."""
    return text.expandtabs(indent)


def fix_trailing_whitespace(text: str) -> str:
    """Strip trailing spaces and tabs from every line."""
    lines = text.splitlines(keepends=True)
    out = []
    for line in lines:
        # Separate the line ending so rstrip doesn't eat it
        if line.endswith('\r\n'):
            out.append(line[:-2].rstrip(' \t') + '\r\n')
        elif line.endswith(('\n', '\r')):
            out.append(line[:-1].rstrip(' \t') + line[-1])
        else:
            out.append(line.rstrip(' \t'))
    return ''.join(out)


def fix_final_newline(text: str) -> str:
    """Ensure the file ends with exactly one newline."""
    if not text:
        return text
    return text.rstrip('\n') + '\n'


def fix_whitespace_after(text: str, masked: str, triggers: str) -> str:
    """
    Insert a space after every character in *triggers* that appears in a
    code region (identified via *masked*) and is immediately followed by a
    non-whitespace character.

    Works character-by-character so position offsets from prior insertions
    never cause mismatches between *text* and *masked*.
    """
    result = []
    n = len(text)
    for i in range(n):
        result.append(text[i])
        if (masked[i] in triggers
                and i + 1 < n
                and masked[i + 1] not in ' \t\n\r'):
            result.append(' ')
    return ''.join(result)


# ---------------------------------------------------------------------------
# Fix pipeline
# ---------------------------------------------------------------------------

def apply_fixes(text: str, indent: int) -> str:
    # Normalise line endings first so all downstream fixers see only \n
    text = fix_line_endings(text)

    # Indentation / whitespace fixes (these alter character positions)
    text = fix_tabs(text, indent)
    text = fix_trailing_whitespace(text)
    text = fix_final_newline(text)

    # Space-after fixes – recompute the mask after the tab expansion above
    masked = mask_literals(text)
    text = fix_whitespace_after(text, masked, ',;')

    return text


# ---------------------------------------------------------------------------
# Entry point
# ---------------------------------------------------------------------------

def main() -> int:
    project_root = Path(__file__).parent
    config_path = project_root / 'config' / 'checkstyle' / 'checkstyle.xml'
    src_root = project_root / 'src'

    indent = read_indent_size(config_path)
    print(f"Checkstyle config : {config_path.relative_to(project_root)}")
    print(f"Indent size       : {indent} spaces")
    print()

    files_changed = 0
    files_checked = 0

    for java_file in sorted(src_root.rglob('*.java')):
        files_checked += 1
        with open(java_file, encoding='utf-8', newline='') as fh:
            original = fh.read()
        fixed = apply_fixes(original, indent)
        if fixed != original:
            with open(java_file, 'w', encoding='utf-8', newline='') as fh:
                fh.write(fixed)
            files_changed += 1
            print(f'  fixed: {java_file.relative_to(project_root)}')

    print(f'\n{files_changed}/{files_checked} files updated.')
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
