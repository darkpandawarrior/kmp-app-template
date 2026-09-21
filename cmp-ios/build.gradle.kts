plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

/**
 * The iOS umbrella module: the one thing `cmp-ios/iosApp.xcodeproj` links against.
 *
 * It produces `ComposeApp.framework` and nothing else. It sits ABOVE `:cmp-shared` and must never
 * be depended on by any other module — that would be a cycle. Surfacing a new API to Swift means
 * adding an `export()`/`api()` pair here, never making a shared module depend on this one.
 *
 * How the three halves meet — the whole iOS wiring, in one place:
 *   1. `baseName = "ComposeApp"`  -> the framework Swift writes `import ComposeApp` for
 *      (cmp-ios/iosApp/ContentView.swift).
 *   2. `MainViewController()` in src/iosMain -> the symbol Swift calls as
 *      `MainViewControllerKt.MainViewController()`. Kotlin's top-level functions land on a
 *      `<FileName>Kt` class, so renaming that FILE renames the Swift call site.
 *   3. Xcode's "Compile Kotlin Framework" build phase runs
 *      `:cmp-ios:embedAndSignAppleFrameworkForXcode`, which writes the framework into
 *      cmp-ios/build/xcode-frameworks/... — the same path FRAMEWORK_SEARCH_PATHS points at.
 * Change any one of those three and you must change its partner, or Xcode fails to link.
 */
kotlin {
    // Only the two targets Compose Multiplatform publishes iOS artifacts for. :cmp-shared also
    // declares iosX64 as a bare Kotlin/Native target, but there is no Compose UI to put in a
    // framework for it — see the target matrix in README.md.
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            // Static: the app links the Kotlin runtime in rather than embedding a dylib, which is
            // what `embedAndSignAppleFrameworkForXcode` and the Xcode phase below are set up for.
            isStatic = true
            // export() only surfaces a module whose dependency is declared `api` below.
            // `dependencies.project(...)`, not `project(...)`: the bare form hands `export` a
            // Project object as a dependency notation, which Gradle 9 deprecates and Gradle 10
            // fails on. The DependencyHandler overload returns a ProjectDependency instead.
            export(dependencies.project(":cmp-shared"))
        }
    }

    sourceSets {
        // Hand-wired rather than using the `iosMain` accessor: gradle.properties sets
        // kotlin.mpp.applyDefaultHierarchyTemplate=false (see :cmp-shared for why), so no
        // intermediate iosMain source set is created for us. Same manual pattern :cmp-shared uses.
        // The name "iosMain" is what gives this source set src/iosMain/kotlin by default.
        // `create(...)`, not `by creating`: the property-delegate source-set syntax is
        // deprecated in Gradle 9 and removed in Gradle 10.
        val iosMain =
            create("iosMain") {
                dependsOn(commonMain.get())
                dependencies {
                    // api(), not implementation(): export() above only works on an `api` dependency.
                    api(project(":cmp-shared"))
                    // ComposeUIViewController lives in compose.ui's UIKit-backed iOS source set.
                    implementation(compose.ui)
                }
            }
        getByName("iosArm64Main").dependsOn(iosMain)
        getByName("iosSimulatorArm64Main").dependsOn(iosMain)
    }
}
