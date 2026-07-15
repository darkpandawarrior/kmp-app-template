pluginManagement {
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
