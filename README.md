# kmp-app-template

A starter for a Kotlin Multiplatform app with one shared Compose UI and four thin shells (Android, Desktop, iOS, Web). Fork it, run the customizer, and you start from a build that already compiles on every target instead of spending the first afternoon aligning Kotlin, Compose, AGP and Gradle.

<!-- AUTOGEN:versions -->
![Kotlin](https://img.shields.io/badge/Kotlin-2.5.0--Beta2-7F52FF?logo=kotlin&logoColor=white)
![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.13.0--beta02-4285F4?logo=jetpackcompose&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-9.9.0--rc--1-02303A?logo=gradle&logoColor=white)
<!-- /AUTOGEN:versions -->

It is the app-shaped member of a small family. [kmp-toolkit](https://github.com/darkpandawarrior/kmp-toolkit) holds the reusable libraries, [kmp-build-logic](https://github.com/darkpandawarrior/kmp-build-logic) holds the Gradle convention plugins, and this repo holds the app that wires both together. Both are git submodules under `external/`. Gradle needs an included build's plugin versions to match the root's, which is why all three repos move to the same Kotlin, Compose and AGP versions together.

## Run it

Clone with submodules, or run `git submodule update --init --recursive` in an existing checkout. You need JDK 25 (CI uses 25 and the daemon is pinned to Amazon's build of it), an Android SDK for the Android shell, and macOS with Xcode for iOS. Put `sdk.dir=...` in `local.properties`.

```bash
git clone --recurse-submodules https://github.com/darkpandawarrior/kmp-app-template.git
cd kmp-app-template

scripts/setup-secrets.sh                         # optional: seed a gitignored secrets.properties
./gradlew :cmp-desktop:run                       # desktop app
./gradlew :cmp-android:assembleDebug             # Android APK
./gradlew :cmp-web:wasmJsBrowserDevelopmentRun   # web app on localhost, live reload
./gradlew :cmp-desktop:hotRunJvm                 # desktop with Compose Hot Reload
open cmp-ios/iosApp.xcodeproj                    # iOS, run from Xcode (it builds :cmp-ios first)
```

What you see on desktop: a splash, then a sign-in screen, then Home. Continue on sign-in only moves the navigation along, there is no account system. The Home screen has an "Ask AI" panel that says plainly when it has no credentials or backend, so no key is needed to look at it. Read [docs/ai-wiring.md](docs/ai-wiring.md) before pointing it at a real model.

There is no hosted demo and no store listing.

## Layout

| Module | Role |
|---|---|
| `:cmp-shared` | `App()`, the one entry point every platform renders, and `RootNavState` (Splash, Login, Home) |
| `:cmp-android` | `MainActivity` calls `App()` |
| `:cmp-desktop` | `main()` opens a `Window { App() }` |
| `:cmp-web` | `main()` calls `ComposeViewport { App() }`, `index.html` loads the bundle |
| `:cmp-ios` | the Gradle umbrella that exports `:cmp-shared` as one static `ComposeApp.framework` and owns `MainViewController()`. The Xcode project in `cmp-ios/` links that framework |

Platform modules own only their entry point. Adding a platform means adding a shell, not rewriting a screen.

Navigation is a plain `when` over `RootDestination`, with no navigation library and no DI container in the way. Swap in `navigation-compose` when you need a back stack. The AI panel does use Koin, bound in `di/AiModule.kt`.

### Targets

| Target | Compose UI | Notes |
|---|---|---|
| Android | yes | `:cmp-android:assembleDebug` |
| Desktop (JVM) | yes | `:cmp-desktop:run` |
| `iosArm64`, `iosSimulatorArm64` | yes | `:cmp-ios` builds the framework |
| `wasmJs` | yes | `:cmp-web:wasmJsBrowserDevelopmentRun` |
| `iosX64` | no | declared as a bare Kotlin/Native target, no Compose artifacts for it |
| `watchosArm64`, `watchosSimulatorArm64` | no | same: shared non-UI code only |

The UI lives in a `composeMain` source set, wired only to the targets Compose Multiplatform supports. To add a target, make its main source set `dependsOn(composeMain)` (see `cmp-shared/build.gradle.kts`) and add a shell that calls `App()`. If Compose does not publish for it, it still gets `commonMain` for non-UI code.

## Make it yours

```bash
scripts/customizer.sh --package com.acme.myapp --name "My App"
```

This rewrites the Kotlin package, the Android `applicationId`, the project name and the iOS bundle identifier in `project.pbxproj`, and moves the source directories to match. It touches every text file, not an extension list, and exits non-zero if a template identifier is left behind. Read the diff and rebuild.

## Previews

`@Preview` works from shared code and five previews ship as examples: two root screens in `App.kt` and three `AiPanel` states in `AiPanel.kt`.

Since Compose Multiplatform 1.10 the annotation is `androidx.compose.ui.tooling.preview.Preview`, from `org.jetbrains.compose.ui:ui-tooling-preview` in `commonMain`. The JetBrains-namespaced one is deprecated, and the `expect`/`actual` preview shim from older blog posts is obsolete. Previews need two artifacts, and missing the second is the usual reason nothing renders:

| Artifact | Role | Goes in |
|---|---|---|
| `ui-tooling-preview` | the annotation | the shared source set |
| `ui-tooling` | the renderer | `androidRuntimeClasspath` only |

Previews are drawn by Android tooling, so a module whose previews you want must keep its Android target, and a preview says nothing about iOS rendering. The renderer runs no effects, so `AiPanel` splits into a stateless `AiPanelContent(uiState, onAsk, onStop)` to be previewable, and `HomeScreen` has no preview because it resolves its backend from Koin. Preview rendering also depends on your IDE supporting the AGP version pinned in `gradle/libs.versions.toml`. Do not downgrade AGP or swap `com.android.kotlin.multiplatform.library` for `com.android.library` to make the panel appear. Use Hot Reload (`hotRunJvm`) for the real screen.

## Checks

```bash
scripts/test-check-ai-wiring.sh   # the wiring guard's own failure cases
scripts/check-ai-wiring.sh        # the guard against the current source
./gradlew :cmp-shared:jvmTest     # AI panel state tests, scripted backend, no paid requests
./gradlew detekt ktlintCheck      # quality half of the gate
./gradlew assemble check          # what CI runs (macOS runner, Apple targets need it)
```

The wiring guard is a grep over marker comments. It stops a fork shipping an unreachable AI endpoint or an engine module nothing uses (reasoning in [docs/DECISIONS.md](docs/DECISIONS.md)).

The version badges above are generated. `scripts/gen-readme.sh` rewrites the span between the `AUTOGEN:versions` markers from `gradle/libs.versions.toml` and the Gradle wrapper, and CI fails if the committed README differs from its output. A badge with no source of truth in the repo would not survive that check, which is why there are only three.

detekt and ktlint apply to every module from the root build and run under `check`. Three ktlint rules are disabled in `.editorconfig` (filename, no-wildcard-imports, property-naming), and `config/detekt/detekt.yml` carries the matching change for each: `MatchingDeclarationName` and `WildcardImport` off, `TopLevelPropertyNaming` set to PascalCase. Line length is owned by ktlint alone at 140, with detekt's `MaxLineLength` off. Change both files together or neither. There is no detekt baseline. A rule that fires means fix the code, or disable the rule with the reason written beside it.

UX standards (edge-to-edge, predictive back, dark theme, reduced motion) are in [DESIGN.md](DESIGN.md).

## Not done yet

- Compose UI for `iosX64` and watchOS, blocked on Compose Multiplatform publishing artifacts.
- A retained-ViewModel variant of the nav scaffold.
- A `navigation-compose` back-stack variant.

Case study for the whole family: [cv-siddharth.vercel.app/project/kmp-family](https://cv-siddharth.vercel.app/project/kmp-family). MIT licence.
