<div align="center">

# kmp-app-template

### A minimal, actually-buildable Kotlin Multiplatform + Compose Multiplatform app seed.

The starting point for a new app in the [kmp-toolkit](https://github.com/darkpandawarrior/kmp-toolkit)
family: one shared Compose UI, a wired root-navigation scaffold, and thin Android + Desktop shells —
nothing you have to delete before you begin. The reusable *library* pieces live in `kmp-toolkit`; this
repo is the reusable *app shape*.

![Kotlin](https://img.shields.io/badge/Kotlin-2.4.20--Beta1-7F52FF?logo=kotlin&logoColor=white)
![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.12.0--beta02-4285F4?logo=jetpackcompose&logoColor=white)
![Platforms](https://img.shields.io/badge/platforms-Android%20%7C%20Desktop-3DDC84)
![Gradle](https://img.shields.io/badge/Gradle-9.7-02303A?logo=gradle&logoColor=white)
![License](https://img.shields.io/badge/license-MIT-blue)

**[Why](#why-kmp-app-template)** · **[What's inside](#whats-inside)** · **[Run it](#run-it)** · **[Make it yours](#make-it-yours)** · **[Roadmap](#roadmap)**

**Portfolio:** [cv-siddharth.vercel.app](https://cv-siddharth.vercel.app/) &nbsp;·&nbsp; **Toolkit:** [kmp-toolkit](https://github.com/darkpandawarrior/kmp-toolkit) &nbsp;·&nbsp; **Build logic:** [kmp-build-logic](https://github.com/darkpandawarrior/kmp-build-logic) &nbsp;·&nbsp; **Sibling apps:** [Mileway](https://github.com/darkpandawarrior/Mileway) · [PaymentsLab](https://github.com/darkpandawarrior/PaymentsLab)

</div>

---

<details>
<summary><b>Table of contents</b></summary>

- [Why kmp-app-template](#why-kmp-app-template)
- [What's inside](#whats-inside)
- [Design choices](#design-choices)
- [Run it](#run-it)
- [Make it yours](#make-it-yours)
- [Adding iOS and Web](#adding-ios-and-web)
- [Tech stack](#tech-stack)
- [Roadmap](#roadmap)

</details>

> **At a glance** — **3-module** Compose Multiplatform seed (`:cmp-shared` · `:cmp-android` · `:cmp-desktop`), root nav state machine wired, **`compileKotlinJvm` + `compileDebugKotlin` verified green** on the pinned toolchain.

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
| `:cmp-shared` | The shared Compose UI. `App()` is the single entry point every platform renders; `RootNavState` is the root navigation state machine (Splash → Login → Home). Targets Android + JVM (Desktop). |
| `:cmp-android` | The Android app shell — `MainActivity` calls `App()`. |
| `:cmp-desktop` | The Desktop app shell — `main()` opens a `Window { App() }`. |

## Design choices

- 🧭 **Navigation with zero ceremony.** The root nav is a clean-room `when`-over-`RootDestination`
  state machine, not a navigation library or a DI framework. That means no extra version matrix to
  align and it compiles anywhere — swap in `navigation-compose` for a back stack, or a retained
  ViewModel + Koin, exactly when the app needs them. The transitions don't change.
- 🧱 **One `App()`, thin shells.** Platform modules own only the entry point (`Activity`, `main()`);
  all UI and logic live in `commonMain`. Adding a platform is adding a shell, never rewriting a screen.
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

./gradlew :cmp-desktop:run            # run the desktop app
./gradlew :cmp-android:assembleDebug  # build the Android APK
```

## Make it yours

```bash
scripts/customizer.sh --package com.acme.myapp --name "My App"
```

Rewrites the Kotlin package, the Android `applicationId`, and the project name across the tree, and
moves the source directories to match. Review the diff and rebuild.

## Adding iOS and Web

`:cmp-shared`'s `App()` is pure `commonMain` Compose, so new targets don't touch the shared code —
add the `iosArm64()` / `iosSimulatorArm64()` and `wasmJs {}` targets to `:cmp-shared`, then a thin
platform shell each (an iOS `UIViewController` calling `App()`, a wasmJs `ComposeViewport { App() }`).

## Tech stack

| | |
|---|---|
| **Language** | Kotlin 2.4.20-Beta1 |
| **UI** | Compose Multiplatform 1.12.0-beta02 |
| **Build** | AGP 9.4.0-alpha04 · Gradle 9.7 |
| **Targets** | Android · Desktop (JVM) — iOS/Web ready to add |
| **License** | MIT |

## Roadmap

- [ ] iOS + Web (wasmJs) shells
- [ ] Optional Koin DI + retained-ViewModel variant of the nav scaffold
- [ ] `navigation-compose` back-stack variant
- [ ] A GitHub Actions build workflow (so this README earns a real CI badge)
- [ ] Wire in a `kmp-toolkit` module or two as a worked example
