#!/usr/bin/env bash
# Guards against "built the backend, never wired the client" (see docs/ai-wiring.md): every server
# AI endpoint needs a client method and a real DI binding; every zero-IO AI engine module needs a
# client-side consumer. The pattern that left three HireSignal AI services unreachable.
#
# Marker-comment based, not routing/DI-syntax based — a syntax-aware check needs one rule per
# routing library and DI framework; a one-line comment is the same shape everywhere.
# ponytail: this trusts the marker, it can't tell a true marker from a pasted one with nothing real
# behind it. Upgrade to parsing actual call graphs if the convention ever gets gamed.
set -euo pipefail

root="${1:-$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)}"
cd "$root"

FIND_ARGS=(. -name '*.kt' -not -path '*/external/*' -not -path '*/build/*')

# All distinct <name>s tagged with marker "$1" (e.g. "ai-endpoint"), across every .kt file.
names_for() {
  find "${FIND_ARGS[@]}" -exec grep -hoE "// *$1:[[:space:]]*[A-Za-z0-9_]+" {} + 2>/dev/null \
    | sed -E "s/.*$1:[[:space:]]*//" | sort -u
}

# True if some .kt file tags "$2" with marker "$1".
has_marker() {
  find "${FIND_ARGS[@]}" -exec grep -lE "// *$1:[[:space:]]*$2([^A-Za-z0-9_]|\$)" {} + 2>/dev/null \
    | grep -q .
}

fail=0

while IFS= read -r name; do
  [ -z "$name" ] && continue
  if ! has_marker "ai-client" "$name"; then
    echo "check-ai-wiring: server AI endpoint '$name' has no ai-client — built the backend, never wired the client." >&2
    fail=1
  fi
  if ! has_marker "ai-di" "$name"; then
    echo "check-ai-wiring: server AI endpoint '$name' has no ai-di binding — nothing wires it into the app." >&2
    fail=1
  fi
done <<EOF
$(names_for "ai-endpoint")
EOF

while IFS= read -r name; do
  [ -z "$name" ] && continue
  if ! has_marker "ai-consumer" "$name"; then
    echo "check-ai-wiring: zero-IO AI engine '$name' has no ai-consumer — built, never called from the client." >&2
    fail=1
  fi
done <<EOF
$(names_for "ai-engine")
EOF

# A non-fake DI binding: the ai-di line itself must not name a test double.
bad_di=$(find "${FIND_ARGS[@]}" -exec grep -nE "// *ai-di:.*(Fake|Mock|Stub)" {} + 2>/dev/null || true)
if [ -n "$bad_di" ]; then
  echo "check-ai-wiring: ai-di binding names a test double — belongs in a test source set, not production DI:" >&2
  echo "$bad_di" >&2
  fail=1
fi

exit "$fail"
