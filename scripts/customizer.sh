#!/usr/bin/env bash
#
# customizer.sh — rename this template into your own app in one shot (template T7).
# Pure bash, zero dependencies. Renames the Kotlin package, the project/app name, and the
# Android applicationId across the whole tree, then moves the source directories to match.
#
# Usage:
#   scripts/customizer.sh --package com.acme.myapp --name "My App"
#
# --package  new root Kotlin package + Android applicationId (e.g. com.acme.myapp)
# --name     human-readable project/app name (e.g. "My App")
#
set -euo pipefail

OLD_PKG="com.siddharth.apptemplate"
OLD_NAME="kmp-app-template"

NEW_PKG=""
NEW_NAME=""
while [[ $# -gt 0 ]]; do
  case "$1" in
    --package) NEW_PKG="$2"; shift 2 ;;
    --name)    NEW_NAME="$2"; shift 2 ;;
    *) echo "unknown arg: $1" >&2; exit 2 ;;
  esac
done

[[ -n "$NEW_PKG" ]] || { echo "--package is required (e.g. com.acme.myapp)" >&2; exit 2; }
[[ -n "$NEW_NAME" ]] || NEW_NAME="$NEW_PKG"

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

echo "Renaming package $OLD_PKG -> $NEW_PKG and name '$OLD_NAME' -> '$NEW_NAME' ..."

OLD_PATH="${OLD_PKG//.//}"
NEW_PATH="${NEW_PKG//.//}"

# Text files worth rewriting: every non-binary file outside git, build output, vendored submodules
# and this script. A grep -I content test (not a file-extension allowlist) is what keeps a new
# file type (.html, .svg, .swift, .plist, .sh ...) from silently keeping the template name, which is
# how the old extension list shipped a fork whose desktop window was still titled "kmp-app-template".
OLD_PASCAL="KmpAppTemplate"
# "My App" -> MyApp; used for the desktop installer's packageName.
NEW_PASCAL="$(printf '%s' "$NEW_NAME" | awk '{ n = split($0, w, /[^A-Za-z0-9]+/); for (i = 1; i <= n; i++) if (w[i] != "") printf "%s%s", toupper(substr(w[i], 1, 1)), substr(w[i], 2) }')"
[[ -n "$NEW_PASCAL" ]] || NEW_PASCAL="App"

# Escape the replacement for sed (| is the delimiter, & and \ are special).
esc() { printf '%s' "$1" | sed -e 's/[\\|&]/\\&/g'; }
E_PKG="$(esc "$NEW_PKG")"; E_NAME="$(esc "$NEW_NAME")"; E_PASCAL="$(esc "$NEW_PASCAL")"

# ponytail: `sed -i ''` is the BSD (macOS) form; on GNU/Linux use `sed -i`.
sedi() { sed -i '' "$@" 2>/dev/null || sed -i "$@"; }

list_text_files() {
  find . \( -path ./.git -o -path ./external -o -path ./.gradle -o -path ./.kotlin -o -path ./graphify-out \
    -o -name build -o -name node_modules \) -prune -o -type f -not -path ./scripts/customizer.sh \
    -not -name '*.jar' -not -name '*.png' -not -name '*.jpg' -print0
}

# 1. Package + app name + PascalCase name. The name rewrite skips any line holding a github.com URL,
# so the README/CAPABILITIES link to the template repo is not turned into "github.com/x/My App".
# .pbxproj is covered here too: the iOS PRODUCT_BUNDLE_IDENTIFIER and CFBundleDisplayName live there.
list_text_files |
  while IFS= read -r -d '' f; do
    grep -IqF -e "$OLD_PKG" -e "$OLD_NAME" -e "$OLD_PASCAL" "$f" || continue
    sedi -e "s|$OLD_PKG|$E_PKG|g" -e "/github\\.com/!s|$OLD_NAME|$E_NAME|g" -e "s|$OLD_PASCAL|$E_PASCAL|g" "$f"
  done

# 2. Move source directories from the old package path to the new one.
find . -type d -path "*/$OLD_PATH" -not -path '*/build/*' -not -path './.git/*' -not -path './external/*' | while read -r dir; do
  newdir="${dir/$OLD_PATH/$NEW_PATH}"
  mkdir -p "$(dirname "$newdir")"
  git mv "$dir" "$newdir" 2>/dev/null || mv "$dir" "$newdir"
done
# Remove the now-empty old parent directories (com/siddharth/apptemplate -> nothing left behind).
find . -type d -empty -path '*/src/*' -not -path '*/build/*' -delete 2>/dev/null || true

# 3. Leftover check: fail loudly if any template identifier survived, instead of shipping it.
LEFT="$(list_text_files | xargs -0 grep -IlF -e "$OLD_PKG" -e "$OLD_PASCAL" -e "$OLD_PATH" 2>/dev/null || true)"
LEFT_NAME="$(list_text_files | xargs -0 grep -IhF "$OLD_NAME" 2>/dev/null | grep -v 'github\.com' || true)"
if [[ -n "$LEFT" || -n "$LEFT_NAME" ]]; then
  echo "WARNING: template identifiers remain:" >&2
  [[ -z "$LEFT" ]] || echo "$LEFT" >&2
  [[ -z "$LEFT_NAME" ]] || echo "$LEFT_NAME" >&2
  exit 1
fi

echo "Done. Review the diff, then: ./gradlew :cmp-desktop:run"
