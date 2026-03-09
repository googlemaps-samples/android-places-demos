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
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Android Places Demos"

include(":PlaceDetailsCompose")
include(":PlaceDetailsUIKit")
include(":PlacesUIKit3D")
include(":demo-java")
include(":demo-kotlin")
include(":kotlin-demos")
include(":snippets")
