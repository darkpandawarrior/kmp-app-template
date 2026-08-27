<div align="center">

<img src="docs/assets/banner.gif" alt="kmp-app-template — a minimal Kotlin Multiplatform + Compose Multiplatform app seed" width="900"/>

### A minimal, actually-buildable Kotlin Multiplatform + Compose Multiplatform app seed.

The starting point for a new app in the [kmp-toolkit](https://github.com/darkpandawarrior/kmp-toolkit)
family: one shared Compose UI, a wired root-navigation scaffold, and thin Android, Desktop, iOS and
Web shells — nothing you have to delete before you begin. The reusable *library* pieces live in
`kmp-toolkit`; this repo is the reusable *app shape*.

![Kotlin](https://img.shields.io/badge/Kotlin-2.4.20--RC-7F52FF?logo=kotlin&logoColor=white)
![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.12.0--rc01-4285F4?logo=jetpackcompose&logoColor=white)
![Platforms](https://img.shields.io/badge/platforms-Android%20%7C%20Desktop%20%7C%20iOS%20%7C%20Web-3DDC84)
![Gradle](https://img.shields.io/badge/Gradle-9.7.0-02303A?logo=gradle&logoColor=white)
![License](https://img.shields.io/badge/license-MIT-blue)

**[Why](#why-kmp-app-template)** · **[What's inside](#whats-inside)** · **[Run it](#run-it)** · **[Make it yours](#make-it-yours)** · **[Roadmap](#roadmap)**

**Case study:** [The KMP family](https://cv-siddharth.vercel.app/project/kmp-family) &nbsp;·&nbsp; **Toolkit:** [kmp-toolkit](https://github.com/darkpandawarrior/kmp-toolkit) &nbsp;·&nbsp; **Build logic:** [kmp-build-logic](https://github.com/darkpandawarrior/kmp-build-logic) &nbsp;·&nbsp; **Sibling apps:** [Mileway](https://github.com/darkpandawarrior/Mileway) · [PaymentsLab](https://github.com/darkpandawarrior/PaymentsLab)

</div>

---

<details>
<summary><b>Table of contents</b></summary>

- [Why kmp-app-template](#why-kmp-app-template)
- [Target matrix](#target-matrix)
- [What's inside](#whats-inside)
- [Design choices](#design-choices)
- [Run it](#run-it)
- [Make it yours](#make-it-yours)
- [Adding a new target](#adding-a-new-target)
- [Tech stack](#tech-stack)
- [Roadmap](#roadmap)

</details>

> **At a glance** — **5-module** Compose Multiplatform seed (`:cmp-shared` · `:cmp-android` · `:cmp-desktop` ·
> `:cmp-web` · `cmp-ios/`), root nav state machine wired, every target below compiles green on the
> pinned toolchain.

## Target matrix

| Platform | Target(s) | Compose UI | Status |
|---|---|---|---|
| Android | `android` | ✅ | `:cmp-android:assembleDebug` |
| Desktop (JVM) | `jvm` | ✅ | `:cmp-desktop:run` |
| iOS | `iosArm64`, `iosSimulatorArm64` | ✅ | `cmp-ios/iosApp.xcodeproj` (real device + Apple Silicon sim) |
| iOS (Intel sim) | `iosX64` | ❌ | compiles as a bare Kotlin/Native target — Compose Multiplatform 1.12.0-beta02 ships no iosX64 artifacts |
| Web | `wasmJs` | ✅ | `:cmp-web:wasmJsBrowserDevelopmentRun` |
| watchOS | `watchosArm64`, `watchosSimulatorArm64`, `watchosX64` | ❌ | compiles as a bare Kotlin/Native target — Compose Multiplatform ships no watchOS artifacts at all |

`App()` (the shared Compose UI) lives in `:cmp-shared`'s `composeMain` source set, which is only wired
to the targets Compose Multiplatform actually supports. iosX64 and watchOS get `commonMain` only —
they're ready for shared non-UI logic today, and pick up Compose automatically the day JetBrains ships
artifacts for them (or drop the targets if you don't need Intel-sim/watch support).

## Why kmp-app-template

Every new Kotlin Multiplatform app starts with the same half-day of yak-shaving: aligning the
Kotlin / Compose / AGP / Gradle version matrix, wiring an entry point per platform, and standing up
navigation before you can render a single screen. This repo is that half-day, done once and kept
green — so a new app starts at "write the feature", not "fight the build".

It's the template arm of the [kmp-toolkit](https://github.com/darkpandawarrior/kmp-toolkit) family:
the toolkit ships the reusable *library* modules (offline-first store, network, security, on-device
AI, and more), `kmp-build-logic` ships the shared Gradle conventions, and this repo ships the *app
shape* they slot into. Its bigger siblings, [Mileway](https://github.com/darkpandawarrior/Mileway)
and [PaymentsLab](https://github.com/darkpandawarrior/PaymentsLab), are what a real app grown from
this seed looks like.

## What's inside

| Module | What it is |
|---|---|
| `:cmp-shared` | The shared Compose UI. `App()` is the single entry point every platform renders; `RootNavState` is the root navigation state machine (Splash → Login → Home). Targets android, jvm, iosArm64, iosSimulatorArm64, iosX64, wasmJs, watchosArm64, watchosSimulatorArm64, watchosX64 — see the [target matrix](#target-matrix) for which ones get the Compose UI. |
| `:cmp-android` | The Android app shell — `MainActivity` calls `App()`. |
| `:cmp-desktop` | The Desktop app shell — `main()` opens a `Window { App() }`. |
| `:cmp-web` | The wasmJs browser shell — `main()` calls `ComposeViewport { App() }`; `index.html` loads the bundle. |
| `cmp-ios/` | The iOS Xcode project (not a Gradle module) — `ContentView.swift` hosts `ComposeUIViewController { App() }` via `cmp-shared`'s `ComposeApp.framework`. |

## Design choices

- 🧭 **Navigation with zero ceremony.** The root nav is a clean-room `when`-over-`RootDestination`
  state machine, not a navigation library or a DI framework. That means no extra version matrix to
  align and it compiles anywhere — swap in `navigation-compose` for a back stack, or a retained
  ViewModel + Koin, exactly when the app needs them. The transitions don't change.
- 🧱 **One `App()`, thin shells.** Platform modules own only the entry point (`Activity`, `main()`,
  `ComposeUIViewController`, `ComposeViewport`); all UI and logic live in `:cmp-shared`. Adding a
  platform is adding a shell, never rewriting a screen.
- 📌 **Pinned, proven toolchain.** Versions come from the family's known-good set (see
  [Tech stack](#tech-stack)), so the seed builds on day one rather than on the day the alphas align.
- 🔧 **Fork-and-go scripts.** `customizer.sh` renames the whole project — package, `applicationId`,
  display name, and source directories — in one command; `setup-secrets.sh` seeds a gitignored
  `secrets.properties` so nothing sensitive ever reaches git.

## Run it

```bash
# One-time: point Gradle at your Android SDK (local.properties is gitignored).
echo "sdk.dir=$HOME/Library/Android/sdk" > local.properties

scripts/setup-secrets.sh              # optional: seed a local secrets.properties

./gradlew :cmp-desktop:run                      # run the desktop app
./gradlew :cmp-android:assembleDebug            # build the Android APK
./gradlew :cmp-web:wasmJsBrowserDevelopmentRun   # run the web app (localhost, live reload)
open cmp-ios/iosApp.xcodeproj                   # run the iOS app from Xcode (⌘R)
```

## Make it yours

```bash
scripts/customizer.sh --package com.acme.myapp --name "My App"
```

Rewrites the Kotlin package, the Android `applicationId`, and the project name across the tree, and
moves the source directories to match. Review the diff and rebuild.

## Adding a new target

`:cmp-shared`'s `App()` lives in a `composeMain` source set, not `commonMain` — wire a new target's
`Main` source set to `dependsOn(composeMain)` (see `cmp-shared/build.gradle.kts`) if Compose
Multiplatform publishes artifacts for it, then add a thin platform shell that calls `App()`. If it
doesn't (yet), the target still gets `commonMain` for shared non-UI logic — see the
[target matrix](#target-matrix)'s iosX64/watchOS rows for that pattern.

## Tech stack

| | |
|---|---|
| **Language** | Kotlin 2.4.20-Beta1 |
| **UI** | Compose Multiplatform 1.12.0-beta02 |
| **Build** | AGP 9.4.0-alpha04 · Gradle 9.7 |
| **Targets** | Android · Desktop (JVM) · iOS (arm64, simulatorArm64, x64) · Web (wasmJs) · watchOS (arm64, simulatorArm64, x64) |
| **License** | MIT |

## Roadmap

- [ ] Compose UI for iosX64/watchOS once Compose Multiplatform publishes artifacts for them
- [ ] Optional Koin DI + retained-ViewModel variant of the nav scaffold
- [ ] `navigation-compose` back-stack variant
- [ ] A GitHub Actions build workflow (so this README earns a real CI badge)
- [ ] Wire in a `kmp-toolkit` module or two as a worked example
