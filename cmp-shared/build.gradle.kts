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
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    // iosArm64/iosSimulatorArm64 get the Compose UI (below); iosX64 is kept as a bare
    // Kotlin/Native target only — Compose Multiplatform 1.12.0-beta02 publishes no iosX64
    // artifacts (org.jetbrains.compose.{runtime,foundation,ui}), so App() can't run there.
    // ponytail: scaffold-only until Compose ships iosX64, or drop it if Intel sim support
    // isn't actually needed.
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
        iosX64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

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
        val composeMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
            }
        }
        androidMain.get().dependsOn(composeMain)
        jvmMain.get().dependsOn(composeMain)
        getByName("wasmJsMain").dependsOn(composeMain)

        // iosArm64/iosSimulatorArm64 only: the ComposeUIViewController entry point (UIKit API,
        // not available on watchOS/other Apple targets).
        val composeIosMain by creating {
            dependsOn(composeMain)
        }
        getByName("iosArm64Main").dependsOn(composeIosMain)
        getByName("iosSimulatorArm64Main").dependsOn(composeIosMain)
    }
}
