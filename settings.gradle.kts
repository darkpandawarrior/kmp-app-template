pluginManagement {
    // Convention plugins kmp-toolkit's own build needs to resolve itself as an included build —
    // same sibling-checkout layout its own settings.gradle.kts documents (external/kmp-toolkit +
    // external/kmp-build-logic side by side).
    includeBuild("external/kmp-build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
    }
}

// fork.project.name lets customizer.sh rename the whole project in one place.
rootProject.name = providers.gradleProperty("fork.project.name").getOrElse("kmp-app-template")

include(":cmp-shared")
include(":cmp-android")
include(":cmp-desktop")
include(":cmp-web")

// The AI stack (:ai/:llm-chat/:result) vendored from kmp-toolkit — pinned SHA in
// external/kmp-toolkit, see that submodule's own commit for what it carries. Only the modules the
// Home AI panel actually uses are substituted; other kmp-toolkit modules stay unresolved on purpose.
includeBuild("external/kmp-toolkit") {
    dependencySubstitution {
        substitute(module("com.siddharth.kmp:ai")).using(project(":ai"))
        substitute(module("com.siddharth.kmp:llm-chat")).using(project(":llm-chat"))
        substitute(module("com.siddharth.kmp:result")).using(project(":result"))
    }
}
