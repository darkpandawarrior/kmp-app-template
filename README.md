# kmp-app-template

A minimal, **buildable** Kotlin Multiplatform + Compose Multiplatform app seed — the starting point
for a new app, with the root navigation scaffold already wired. Extracted as the template arm of the
`kmp-toolkit` family (backlog T5); the reusable library pieces live in `kmp-toolkit`, the app shape
lives here.

## What's inside

| Module | What it is |
|---|---|
| `:cmp-shared` | The shared Compose UI + `App()` entry point + the root nav state machine (`RootNavState`: Splash → Login → Home). Targets android + jvm (desktop). |
| `:cmp-android` | The Android app shell — `MainActivity` calls `App()`. |
| `:cmp-desktop` | The desktop app shell — `main()` opens a `Window { App() }`. |

The navigation is a clean-room `when`-over-`RootDestination` state machine — no navigation library, no
DI framework, so it compiles anywhere with zero version-matrix friction. Swap in `navigation-compose`
for a back stack, or a retained ViewModel + Koin, when the app needs them; the transitions stay the same.

## Run it

```bash
# Android SDK: create local.properties with `sdk.dir=/path/to/Android/sdk` (gitignored).
scripts/setup-secrets.sh              # seed a local secrets.properties (optional)

./gradlew :cmp-desktop:run            # desktop
./gradlew :cmp-android:assembleDebug  # android APK
```

Toolchain (all pinned in `gradle/libs.versions.toml`): Kotlin 2.4.20-Beta1 · Compose Multiplatform
1.12.0-beta02 · AGP 9.4.0-alpha04 · Gradle 9.7.

## Make it yours

```bash
scripts/customizer.sh --package com.acme.myapp --name "My App"
```

Renames the Kotlin package, the Android `applicationId`, and the project name across the tree, and moves
the source directories to match. Review the diff and rebuild.

## Adding iOS / Web

`:cmp-shared`'s `App()` is pure common Compose, so an iOS (`UIViewController`) and a wasmJs (`ComposeViewport`)
shell drop in without touching the shared code — add the `iosArm64()/iosSimulatorArm64()` and `wasmJs {}`
targets to `:cmp-shared` plus a thin platform module each.
