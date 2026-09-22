plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.androidKmpLibrary) apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    // Code quality — applied to the root so `./gradlew detekt ktlintCheck` has a task here,
    // and propagated to every module by the `subprojects` block below. Both plugins register
    // their check tasks as dependencies of `check`, which is what CI already runs.
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "dev.detekt")
    extensions.configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        // Never lint generated code (Compose resource accessors, KSP output).
        filter { exclude { entry -> entry.file.path.contains("/build/") } }
    }
    extensions.configure<dev.detekt.gradle.extensions.DetektExtension> {
        config.setFrom(rootProject.files("config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
        // The plain `detekt` task binds to the JVM `main` source set, which a KMP module does not
        // have — leave it at the default and detekt is NO-SOURCE everywhere, reporting a clean
        // repo it never opened. `src` rather than an enumerated list of source sets: adding a
        // target adds a source set, and nothing fails when a list is not updated to match, so
        // coverage would shrink silently while the build stayed green.
        //
        // This is ALSO the whole generated-code exclusion. KSP and Compose-resources output lands
        // under `build/generated/`, never under `src/`, so a `src`-rooted detekt cannot reach it —
        // which is why there is no `exclude("**/build/generated/**")` here to go stale. The same
        // globs still appear per rule in config/detekt/detekt.yml, because a fork that switches to
        // the per-source-set `detekt*SourceSet` tasks DOES pick the generated dirs up (Gradle
        // declares them as source) and would otherwise lint machine-written names. ktlint runs off
        // those declared source sets already, hence its `/build/` filter above.
        source.setFrom(layout.projectDirectory.dir("src"))
    }
}
