# Decisions log

Settled choices for this repo, one line each, so a new session does not re-open them. Read before proposing an architectural change, and append a line when a decision is made.

- 2026-09-06: CI runs scripts/check-ai-wiring.sh, a marker-comment grep, instead of a routing-aware or DI-aware check. Why: every server AI endpoint needs a client method and a non-fake DI binding, every zero-IO engine needs a consumer, and a grep holds regardless of which routing or DI library a fork uses. A repo with no markers passes. (source: 240b2da, docs/ai-wiring.md)
- 2026-09-20: A single :cmp-ios umbrella module owns the one static ComposeApp.framework, and :cmp-shared no longer declares a framework. Why: the Xcode project had reached around the module boundary into :cmp-shared, and one framework means one answer to what Xcode links. (source: 6fc3934)
- 2026-09-20: customizer.sh also rewrites project.pbxproj. Why: the iOS bundle identifier and display name live only there, so each fork would otherwise ship as com.siddharth.apptemplate.ios. (source: 6fc3934)
- 2026-09-22: detekt and ktlint are wired in at the family standard, with config/detekt/detekt.yml byte-identical to the sibling repos. Why: the repo had no detekt at all, so its zero baseline meant unmeasured rather than clean. (source: b358c77)
- 2026-10-19: UX standards are part of the template: edge-to-edge, predictive back, system dark theme and reduced motion are wired in, and no new screen, flow or motion lands without 2 to 3 real-app references. Why: new apps start from these baselines, and "looks modern" is not a reference. (source: 51e1a42, DESIGN.md Standards section)
