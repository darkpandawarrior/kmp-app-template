#!/usr/bin/env bash
# Regenerates ONLY the <!-- AUTOGEN:x --> … <!-- /AUTOGEN:x --> spans in README.md from
# source-of-truth in the repo. Hand-written prose outside the markers is never touched.
# Run locally (./scripts/gen-readme.sh) or in CI (.github/workflows/ci.yml verifies it is current).
#
# WHY THIS EXISTS: a hand-typed version badge is the least checkable claim a README can make. It
# renders as authority and nothing in the build ever compares it to reality. Measured across this
# family before this script was written, six of nine repos were advertising a Kotlin RC and a
# Compose Multiplatform version a full minor behind their own catalog. The badge is not decoration;
# it is the first thing a reader believes.
#
# FORK NOTE: add a badge by adding a variable and a line to the $versions block below, then run the
# script. If a fork adds a badge whose value has no source of truth in the repo (a platform list, a
# licence), leave it OUTSIDE the marker span — this script only owns claims it can verify.
#
# ponytail: sed + perl block-replace over one marker, no templating engine, no Node, no Gradle task.
set -euo pipefail
cd "$(dirname "$0")/.."

README="README.md"
CATALOG="gradle/libs.versions.toml"
WRAPPER="gradle/wrapper/gradle-wrapper.properties"

catalog_version() { # $1 = TOML key at the start of a line in [versions]
  sed -n "s/^$1[[:space:]]*=[[:space:]]*\"\([^\"]*\)\".*/\1/p" "$CATALOG" | head -1
}
# shields.io escaping: a literal '-' in a badge's message has to be doubled, or the badge splits.
shield() { printf '%s' "${1//-/--}"; }

kotlin_v=$(shield "$(catalog_version kotlin)")
cmp_v=$(shield "$(catalog_version compose-multiplatform)")
gradle_v=$(shield "$(sed -n 's/.*gradle-\(.*\)-bin\.zip/\1/p' "$WRAPPER" | head -1)")

versions="<!-- AUTOGEN:versions -->
![Kotlin](https://img.shields.io/badge/Kotlin-${kotlin_v}-7F52FF?logo=kotlin&logoColor=white)
![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-${cmp_v}-4285F4?logo=jetpackcompose&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-${gradle_v}-02303A?logo=gradle&logoColor=white)
<!-- /AUTOGEN:versions -->"

replace_block() {   # $1=tag  $2=replacement (marker lines included)
  TAG="$1" REPL="$2" perl -0777 -i -pe '
    s/<!-- AUTOGEN:\Q$ENV{TAG}\E -->.*?<!-- \/AUTOGEN:\Q$ENV{TAG}\E -->/$ENV{REPL}/s;
  ' "$README"
}

replace_block "versions" "$versions"
echo "[gen-readme] kotlin=$kotlin_v compose-multiplatform=$cmp_v gradle=$gradle_v"
