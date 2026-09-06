#!/usr/bin/env bash
# The one runnable check for check-ai-wiring.sh's own branching logic: fixture repos with a known
# pass/fail shape, asserted against the script's actual exit code. Not wired into `assemble check`
# (it has nothing to do with compiling the app) — run directly, and from ci.yml.
set -euo pipefail

here="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
checker="$here/check-ai-wiring.sh"
fixtures="$(mktemp -d)"
trap 'rm -rf "$fixtures"' EXIT

fail=0
assert_exit() {
  local case_dir="$1" want="$2"
  local got=0
  bash "$checker" "$case_dir" >/dev/null 2>&1 || got=$?
  if [ "$got" -ne "$want" ]; then
    echo "FAIL: $case_dir expected exit $want, got $got" >&2
    fail=1
  else
    echo "ok: $case_dir -> exit $got"
  fi
}

mkdir -p "$fixtures"/{pass,fail-endpoint,fail-engine,fail-fakedi,empty}

cat > "$fixtures/pass/Server.kt" << 'EOF'
// ai-endpoint: Summarize
fun summarizeRoute() {}
EOF
cat > "$fixtures/pass/Client.kt" << 'EOF'
// ai-client: Summarize
fun callSummarize() {}
// ai-di: Summarize
val binding = RealSummarizer()
// ai-engine: Ranker
class Ranker
EOF
cat > "$fixtures/pass/Consumer.kt" << 'EOF'
// ai-consumer: Ranker
fun useRanker() = Ranker()
EOF

cat > "$fixtures/fail-endpoint/Server.kt" << 'EOF'
// ai-endpoint: Orphan
fun orphanRoute() {}
EOF

cat > "$fixtures/fail-engine/Engine.kt" << 'EOF'
// ai-engine: LonelyEngine
class LonelyEngine
EOF

cat > "$fixtures/fail-fakedi/Server.kt" << 'EOF'
// ai-endpoint: Chat
fun chatRoute() {}
EOF
cat > "$fixtures/fail-fakedi/Client.kt" << 'EOF'
// ai-client: Chat
fun callChat() {}
// ai-di: Chat FakeChatService
val binding = FakeChatService()
EOF

assert_exit "$fixtures/pass" 0
assert_exit "$fixtures/fail-endpoint" 1
assert_exit "$fixtures/fail-engine" 1
assert_exit "$fixtures/fail-fakedi" 1
assert_exit "$fixtures/empty" 0

exit "$fail"
