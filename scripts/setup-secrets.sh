#!/usr/bin/env bash
#
# setup-secrets.sh — Path-A secrets bootstrap (template T7). Pure bash, zero dependencies.
# Seeds a gitignored secrets.properties from the committed template, so a fresh clone has a local,
# private place to put keys without anything sensitive ever entering git.
#
# Usage: scripts/setup-secrets.sh
#
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

TEMPLATE="secrets.properties.template"
TARGET="secrets.properties"

[[ -f "$TEMPLATE" ]] || { echo "missing $TEMPLATE" >&2; exit 1; }

if [[ -f "$TARGET" ]]; then
  echo "$TARGET already exists — leaving it untouched."
  exit 0
fi

cp "$TEMPLATE" "$TARGET"
echo "Created $TARGET from $TEMPLATE. Fill in the values — it is gitignored and stays local."
