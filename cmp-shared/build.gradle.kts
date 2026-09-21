import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvm()

    // Library target — :cmp-web owns the executable() + index.html; browser() here just lets
    // the Kotlin/Wasm tooling (npm install, distribution tasks) see this as a JS-consuming module.
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        // Required for wasmJsBrowserTest: without a declared executable, the Compose Gradle plugin's
        // Skiko-runtime check fails the test task outright (CMP-4906) since Compose UI can't load its
        // renderer from a bare klib. Compose Multiplatform 1.12.0-rc01 promoted that check to a
        // hard build failure (`checkComposeUiTestConfigurationForWasmJs`), so this is no longer
        // optional. Same fix kmp-toolkit's :designsystem already carries.
        binaries.executable()
    }

    android {
        namespace = "com.siddharth.apptemplate.shared"
        compileSdk =
            libs.versions.android.compileSdk
                .get()
                .toInt()
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
    }

    // This module declares the iOS TARGETS but produces no framework — :cmp-ios is the umbrella
    // that does that, and it export()s this module. Keeping exactly one framework in the build
    // means there is exactly one answer to "what does Xcode link against".
    //
    // iosArm64/iosSimulatorArm64 get the Compose UI (below); iosX64 is kept as a bare
    // Kotlin/Native target only — Compose Multiplatform publishes no iosX64 artifacts
    // (org.jetbrains.compose.{runtime,foundation,ui}), so App() can't run there.
    // ponytail: scaffold-only until Compose ships iosX64, or drop it if Intel sim support
    // isn't actually needed.
    iosArm64()
    iosSimulatorArm64()
    iosX64()

    // Compile targets only, same reason as iosX64 above — Compose Multiplatform publishes no
    // watchOS artifacts at all. Ready for shared non-UI logic (commonMain); no UI shell.
    watchosArm64()
    watchosSimulatorArm64()
    watchosX64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }

        // Compose UI lives here, not in commonMain, so it's only on the classpath of targets
        // Compose Multiplatform actually supports (android, jvm, iosArm64, iosSimulatorArm64).
        //
        // The AI stack (:ai/:llm-chat/:result, vendored from external/kmp-toolkit) lives here too,
        // for the same reason: its targets (android, jvm, iosArm64, iosSimulatorArm64, wasmJs) are
        // exactly composeMain's target set — watchOS/iosX64 get neither Compose UI nor the AI panel
        // that renders through it, so a commonMain dependency here would fail to resolve for them.
        // `create(...)`, not `by creating`: the property-delegate source-set syntax is
        // deprecated in Gradle 9 and removed in Gradle 10.
        val composeMain =
            create("composeMain") {
                dependsOn(commonMain.get())
                dependencies {
                    implementation(compose.runtime)
                    implementation(compose.foundation)
                    implementation(compose.material3)
                    implementation(compose.ui)
                    implementation("com.siddharth.kmp:ai:1.0.0")
                    implementation("com.siddharth.kmp:llm-chat:1.0.0")
                    implementation("com.siddharth.kmp:result:1.0.0")
                    implementation(libs.koin.core)
                }
            }
        androidMain.get().dependsOn(composeMain)
        jvmMain.get().dependsOn(composeMain)
        getByName("wasmJsMain").dependsOn(composeMain)

        // The two iOS targets Compose Multiplatform publishes artifacts for, so App() compiles
        // for them. The UIKit entry point that wraps App() in a UIViewController lives in
        // :cmp-ios (MainViewController.kt), not here — this module stays UIKit-free.
        getByName("iosArm64Main").dependsOn(composeMain)
        getByName("iosSimulatorArm64Main").dependsOn(composeMain)

        // AiPanelStateTest lives here, not commonTest: it exercises composeMain-only symbols
        // (AiPanelState depends on :ai/:llm-chat, which don't publish watchOS/iosX64 targets — see
        // composeMain's own comment above), and jvmTest already sees jvmMain's full dependency
        // graph without a new source-set hierarchy.
        jvmTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
