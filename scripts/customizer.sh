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

# 1. Rewrite references in text files (skip build output, git, the wrapper jar, and this script).
# .pbxproj is in the list because the iOS app's PRODUCT_BUNDLE_IDENTIFIER lives there and nowhere
# else — a fork that skipped it would ship with com.siddharth.apptemplate.ios as its bundle id.
find . \
  -type f \( -name '*.kt' -o -name '*.kts' -o -name '*.xml' -o -name '*.toml' -o -name '*.properties' -o -name '*.md' -o -name '*.pbxproj' \) \
  -not -path './.git/*' -not -path '*/build/*' -not -path './scripts/customizer.sh' -print0 |
  while IFS= read -r -d '' f; do
    # ponytail: `sed -i ''` is the BSD (macOS) form; on GNU/Linux use `sed -i`.
    sed -i '' -e "s|$OLD_PKG|$NEW_PKG|g" "$f" 2>/dev/null || sed -i -e "s|$OLD_PKG|$NEW_PKG|g" "$f"
  done

# 2. Move source directories from the old package path to the new one.
find . -type d -path "*/$OLD_PATH" -not -path '*/build/*' -not -path './.git/*' | while read -r dir; do
  newdir="${dir/$OLD_PATH/$NEW_PATH}"
  mkdir -p "$(dirname "$newdir")"
  git mv "$dir" "$newdir" 2>/dev/null || mv "$dir" "$newdir"
done

# 3. Project/app display name. The .pbxproj carries it as INFOPLIST_KEY_CFBundleDisplayName —
# that is the name under the icon on an iPhone home screen, so it has to move with the rest.
NAME_FILES=(gradle.properties settings.gradle.kts README.md cmp-ios/iosApp.xcodeproj/project.pbxproj)
sed -i '' -e "s|$OLD_NAME|$NEW_NAME|g" "${NAME_FILES[@]}" 2>/dev/null || \
  sed -i -e "s|$OLD_NAME|$NEW_NAME|g" "${NAME_FILES[@]}"

echo "Done. Review the diff, then: ./gradlew :cmp-desktop:run"
