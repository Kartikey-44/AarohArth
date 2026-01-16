pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        // REQUIRED for MPAndroidChart
        maven(url = "https://jitpack.io")
    }
}

rootProject.name = "Aaroh Arth"
include(":app")
