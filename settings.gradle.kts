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
<<<<<<< HEAD
=======
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
>>>>>>> 875cc9a843b00cb19a93e420b3d465078607f08c
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

<<<<<<< HEAD
rootProject.name = "WorkoutTrackerApp"
=======
rootProject.name = "FitPro"
>>>>>>> 875cc9a843b00cb19a93e420b3d465078607f08c
include(":app")
