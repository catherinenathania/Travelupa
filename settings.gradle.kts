// settings.gradle.kts
pluginManagement {
    repositories {
        // PERBAIKAN: Hapus blok 'content' yang membatasi resolusi plugin
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
    }
}

rootProject.name = "Travelupa"
include(":app")