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
project(":PlaceDetailsCompose").projectDir = file("PlaceDetailsCompose/app")

include(":PlaceDetailsUIKit")
project(":PlaceDetailsUIKit").projectDir = file("PlaceDetailsUIKit/app")

include(":PlacesUIKit3D")
project(":PlacesUIKit3D").projectDir = file("PlacesUIKit3D/app")

include(":demo-java")
project(":demo-java").projectDir = file("demo-java/app")

include(":demo-kotlin")
project(":demo-kotlin").projectDir = file("demo-kotlin/app")

include(":kotlin-demos")
project(":kotlin-demos").projectDir = file("kotlin-demos/app")

include(":snippets")
project(":snippets").projectDir = file("snippets/app")
