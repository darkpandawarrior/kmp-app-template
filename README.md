<div align="center">

<img src="docs/assets/banner.gif" alt="kmp-app-template, a minimal Kotlin Multiplatform + Compose Multiplatform app seed" width="900"/>

### A minimal, actually-buildable Kotlin Multiplatform + Compose Multiplatform app seed.

The starting point for a new app in the [kmp-toolkit](https://github.com/darkpandawarrior/kmp-toolkit)
family: one shared Compose UI, a wired root-navigation scaffold, and thin Android, Desktop, iOS and
Web shells, nothing you have to delete before you begin. The reusable *library* pieces live in
`kmp-toolkit`; this repo is the reusable *app shape*.

<!-- AUTOGEN:versions -->
![Kotlin](https://img.shields.io/badge/Kotlin-2.4.20-7F52FF?logo=kotlin&logoColor=white)
![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.13.0--alpha01-4285F4?logo=jetpackcompose&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-9.8.0--rc--2-02303A?logo=gradle&logoColor=white)
<!-- /AUTOGEN:versions -->
![Platforms](https://img.shields.io/badge/platforms-Android%20%7C%20Desktop%20%7C%20iOS%20%7C%20Web-3DDC84)
![License](https://img.shields.io/badge/license-MIT-blue)

**[Why](#why-kmp-app-template)** · **[What's inside](#whats-inside)** · **[Run it](#run-it)** · **[Previews](#previews-and-live-ui-iteration)** · **[Make it yours](#make-it-yours)** · **[Roadmap](#roadmap)**

**Case study:** [The KMP family](https://siddharth-pandalai.vercel.app/project/kmp-family) ([mirror](https://cv-siddharth.vercel.app/project/kmp-family)) &nbsp;·&nbsp; **Toolkit:** [kmp-toolkit](https://github.com/darkpandawarrior/kmp-toolkit) &nbsp;·&nbsp; **Build logic:** [kmp-build-logic](https://github.com/darkpandawarrior/kmp-build-logic) &nbsp;·&nbsp; **Sibling apps:** [Doori](https://github.com/darkpandawarrior/Doori) · [Gaddi](https://github.com/darkpandawarrior/Gaddi) · [PaymentsLab-KMP](https://github.com/darkpandawarrior/PaymentsLab-KMP) · [Candidai](https://github.com/darkpandawarrior/Candidai)

</div>

---

<details>
<summary><b>Table of contents</b></summary>

- [Why kmp-app-template](#why-kmp-app-template)
- [Target matrix](#target-matrix)
- [What's inside](#whats-inside)
- [Design choices](#design-choices)
- [Run it](#run-it)
- [Previews and live UI iteration](#previews-and-live-ui-iteration)
- [Make it yours](#make-it-yours)
- [Adding a new target](#adding-a-new-target)
- [Tech stack](#tech-stack)
- [Roadmap](#roadmap)

</details>

> **At a glance**, **5-module** Compose Multiplatform seed (`:cmp-shared` · `:cmp-android` · `:cmp-desktop` ·
> `:cmp-web` · `:cmp-ios`), root nav state machine wired, every target below compiles green on the
> pinned toolchain.

## Target matrix

| Platform | Target(s) | Compose UI | Status |
|---|---|---|---|
| Android | `android` | ✅ | `:cmp-android:assembleDebug` |
| Desktop (JVM) | `jvm` | ✅ | `:cmp-desktop:run` |
| iOS | `iosArm64`, `iosSimulatorArm64` | ✅ | `:cmp-ios` builds `ComposeApp.framework`; `cmp-ios/iosApp.xcodeproj` links it (real device + Apple Silicon sim) |
| iOS (Intel sim) | `iosX64` | ❌ | compiles as a bare Kotlin/Native target, Compose Multiplatform 1.13.0-alpha01 ships no iosX64 artifacts |
| Web | `wasmJs` | ✅ | `:cmp-web:wasmJsBrowserDevelopmentRun` |
| watchOS | `watchosArm64`, `watchosSimulatorArm64`, `watchosX64` | ❌ | compiles as a bare Kotlin/Native target, Compose Multiplatform ships no watchOS artifacts at all |

`App()` (the shared Compose UI) lives in `:cmp-shared`'s `composeMain` source set, which is only wired
to the targets Compose Multiplatform actually supports. iosX64 and watchOS get `commonMain` only
they're ready for shared non-UI logic today, and pick up Compose automatically the day JetBrains ships
artifacts for them (or drop the targets if you don't need Intel-sim/watch support).

## Why kmp-app-template

Every new Kotlin Multiplatform app starts with the same half-day of yak-shaving: aligning the
Kotlin / Compose / AGP / Gradle version matrix, wiring an entry point per platform, and standing up
navigation before you can render a single screen. This repo is that half-day, done once and kept
green, so a new app starts at "write the feature", not "fight the build".

It's the template arm of the [kmp-toolkit](https://github.com/darkpandawarrior/kmp-toolkit) family:
the toolkit ships the reusable *library* modules (offline-first store, network, security, on-device
AI, and more), `kmp-build-logic` ships the shared Gradle conventions, and this repo ships the *app
shape* they slot into. Its bigger siblings, [Doori](https://github.com/darkpandawarrior/Doori),
[Gaddi](https://github.com/darkpandawarrior/Gaddi),
[PaymentsLab-KMP](https://github.com/darkpandawarrior/PaymentsLab-KMP) and
[Candidai](https://github.com/darkpandawarrior/Candidai), are what a real app grown from this seed
looks like. All four, `kmp-toolkit` and `kmp-build-logic` build against the same version triple as
this template — see [Tech stack](#tech-stack) — since Gradle requires an included build's plugin
versions to match the root's; a repo that drifts from the triple can't resolve the toolkit composite
at all.

## What's inside

| Module | What it is |
|---|---|
| `:cmp-shared` | The shared Compose UI. `App()` is the single entry point every platform renders; `RootNavState` is the root navigation state machine (Splash → Login → Home). Targets android, jvm, iosArm64, iosSimulatorArm64, iosX64, wasmJs, watchosArm64, watchosSimulatorArm64, watchosX64, see the [target matrix](#target-matrix) for which ones get the Compose UI. |
| `:cmp-android` | The Android app shell, `MainActivity` calls `App()`. |
| `:cmp-desktop` | The Desktop app shell, `main()` opens a `Window { App() }`. |
| `:cmp-web` | The wasmJs browser shell, `main()` calls `ComposeViewport { App() }`; `index.html` loads the bundle. |
| `:cmp-ios` | The iOS app shell, in two halves. The Gradle module is the umbrella that exports `:cmp-shared` into a single static `ComposeApp.framework`, and owns `MainViewController()` — the `ComposeUIViewController { App() }` entry point. Alongside it, `cmp-ios/iosApp.xcodeproj` is the Xcode app that builds that framework from a build phase and links it. |

## Design choices

- 🧭 **Navigation with zero ceremony.** The root nav is a clean-room `when`-over-`RootDestination`
  state machine, not a navigation library or a DI framework. That means no extra version matrix to
  align and it compiles anywhere, swap in `navigation-compose` for a back stack, or a retained
  ViewModel + Koin, exactly when the app needs them. The transitions don't change.
- 🧱 **One `App()`, thin shells.** Platform modules own only the entry point (`Activity`, `main()`,
  `ComposeUIViewController`, `ComposeViewport`); all UI and logic live in `:cmp-shared`. Adding a
  platform is adding a shell, never rewriting a screen.
- 📌 **Pinned, proven toolchain.** Versions come from the family's known-good set (see
  [Tech stack](#tech-stack)), so the seed builds on day one rather than on the day the alphas align.
- 🔧 **Fork-and-go scripts.** `customizer.sh` renames the whole project, package, `applicationId`,
  display name, and source directories, in one command; `setup-secrets.sh` seeds a gitignored
  `secrets.properties` so nothing sensitive ever reaches git.

## Run it

```bash
# One-time: point Gradle at your Android SDK (local.properties is gitignored).
echo "sdk.dir=$HOME/Library/Android/sdk" > local.properties

scripts/setup-secrets.sh              # optional: seed a local secrets.properties

./gradlew :cmp-desktop:run                      # run the desktop app
./gradlew :cmp-android:assembleDebug            # build the Android APK
./gradlew :cmp-web:wasmJsBrowserDevelopmentRun   # run the web app (localhost, live reload)
./gradlew :cmp-desktop:hotRunJvm                # run desktop with Compose Hot Reload (see below)
open cmp-ios/iosApp.xcodeproj                   # run the iOS app from Xcode (⌘R — builds :cmp-ios first)
```

## Previews and live UI iteration

`@Preview` works in shared Compose code, and the wiring is already in place. Five previews ship as
the worked example: two root screens in `App.kt`, three `AiPanel` states in `AiPanel.kt`.

**The annotation flipped in Compose Multiplatform 1.10, and most material online predates that.**
The multiplatform annotation is now `androidx.compose.ui.tooling.preview.Preview`, published into
`commonMain` by `org.jetbrains.compose.ui:ui-tooling-preview`. The JetBrains-namespaced
`org.jetbrains.compose.ui.tooling.preview.Preview` is the *deprecated* one, and the
`expect`/`actual` "Preview shim" that 2024-era blog posts recommend is obsolete — delete it if you
find one in a fork.

**Previews need two artifacts, not one**, which is the usual reason they render nothing:

| Artifact | Role | Where it goes |
|---|---|---|
| `org.jetbrains.compose.ui:ui-tooling-preview` | the `@Preview` **annotation** | the shared source set (`composeMain` here) |
| `org.jetbrains.compose.ui:ui-tooling` | the **renderer** | `androidRuntimeClasspath` only |

Both are in `cmp-shared/build.gradle.kts` with a fork note on each. The renderer is Android-only
because previews are drawn by the Android preview tooling even when the composable is shared — so
**a module whose previews you want to see must keep its Android target**, and a preview tells you
about the *Android* rendering of shared code, never about iOS pixel fidelity.

Two more things a fork should know:

- **Hoist state to make a screen previewable.** `AiPanel` delegates to a private, stateless
  `AiPanelContent(uiState, onAsk, onStop)`. That split is the only reason its three states can be
  previewed at all: the IDE preview renderer runs no effects, so anything driven by a coroutine
  stays stuck on its initial value. `HomeScreen` deliberately has *no* preview — it resolves its
  backend out of Koin, and starting DI inside the renderer is not worth it. Preview the leaves.
- **Previews are not the desktop loop.** `./gradlew :cmp-desktop:hotRunJvm` starts the desktop app
  with Compose Hot Reload (bundled and on by default since Compose Multiplatform 1.10 — no plugin,
  no dependency): edit a composable, save, the running UI swaps without losing state. Use previews
  for isolated states, Hot Reload for iterating on the real screen.

> **IDE note.** Preview rendering is IDE-side and gated on the IDE understanding your AGP. This
> template pins AGP 9.5.0-alpha06, which is above the AGP ceiling of the current *stable* Android
> Studio — the preview panel needs the canary/RC line that pairs with AGP 9.5. Do not downgrade AGP
> or swap `com.android.kotlin.multiplatform.library` back to `com.android.library` to make the
> panel appear; install the matching IDE instead, and use Hot Reload in the meantime.

## Make it yours

```bash
scripts/customizer.sh --package com.acme.myapp --name "My App"
```

Rewrites the Kotlin package, the Android `applicationId`, and the project name across the tree, and
moves the source directories to match. Review the diff and rebuild.

## Code quality

The README's Kotlin, Compose Multiplatform and Gradle badges are **generated**, not typed:
`scripts/gen-readme.sh` rewrites the `<!-- AUTOGEN:versions -->` span from
`gradle/libs.versions.toml` and the Gradle wrapper, and CI fails a change that leaves them stale.
Measured across this family before the script existed, six of nine repos were advertising a Kotlin
RC and a Compose Multiplatform version a full minor behind their own catalog — a badge renders as
authority and nothing in a build ever checks it. A fork adds a badge by adding a line to the script;
a badge with no source of truth in the repo (the platform list, the licence) stays outside the span.


detekt and ktlint are applied to every module from the root build and hook into `check`, so the
CI gate is just `./gradlew assemble check` — there is no separate lint job to forget.

```bash
./gradlew detekt ktlintCheck    # the quality half of the gate on its own
./gradlew ktlintFormat          # fix what is mechanically fixable
```

Two files carry the whole configuration, and every rule that is off carries the reason it is off:

| File | Owns |
|---|---|
| `config/detekt/detekt.yml` | Rule thresholds, the `*notOurs` exclusion anchor, the `@Composable`/`@Preview` ignores. |
| `.editorconfig` | Formatting, and `max_line_length` — the only place that number lives. |

Four ktlint rules are disabled in `.editorconfig` and each one is **half a pair**: detekt turns the
same rule off in `detekt.yml`. Change one side without the other and the two tools start
contradicting each other. The comments name the pairing at both ends.

**There is no detekt baseline here, and adding one is the wrong fix.** A baseline entry is debt that
outlives whoever added it; this repo starts at zero findings so a fork inherits a gate that means
something. If a rule fires, fix the code, or turn the rule off with the reason written next to it.

## Adding a new target

`:cmp-shared`'s `App()` lives in a `composeMain` source set, not `commonMain`, wire a new target's
`Main` source set to `dependsOn(composeMain)` (see `cmp-shared/build.gradle.kts`) if Compose
Multiplatform publishes artifacts for it, then add a thin platform shell that calls `App()`. If it
doesn't (yet), the target still gets `commonMain` for shared non-UI logic, see the
[target matrix](#target-matrix)'s iosX64/watchOS rows for that pattern.

## Tech stack

| | |
|---|---|
| **Language** | Kotlin 2.4.20 |
| **UI** | Compose Multiplatform 1.13.0-alpha01 |
| **Build** | AGP 9.5.0-alpha06 · Gradle 9.8.0-rc-2 |
| **Targets** | Android · Desktop (JVM) · iOS (arm64, simulatorArm64, x64) · Web (wasmJs) · watchOS (arm64, simulatorArm64, x64) |
| **License** | MIT |

## Roadmap

- [ ] Compose UI for iosX64/watchOS once Compose Multiplatform publishes artifacts for them
- [ ] Optional Koin DI + retained-ViewModel variant of the nav scaffold
- [ ] `navigation-compose` back-stack variant
- [x] A GitHub Actions build workflow (see [`ci.yml`](.github/workflows/ci.yml))
- [x] Wire in a `kmp-toolkit` module or two as a worked example — `:ai`/`:llm-chat`/`:result`,
      the Home screen's "Ask AI" panel (see [`AiPanel.kt`](cmp-shared/src/composeMain/kotlin/com/siddharth/apptemplate/shared/ai/AiPanel.kt))
- [x] Grep-based guard so a forked app can't ship an unreachable AI endpoint or engine module
      (see [`docs/ai-wiring.md`](docs/ai-wiring.md))
