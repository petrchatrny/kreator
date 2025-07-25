pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "kreator"
include(":kreator-annotations")
include(":kreator-compiler")
include(":test-jvm")
include(":test-kmp")
