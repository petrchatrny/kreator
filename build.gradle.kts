plugins {
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply  false
    alias(libs.plugins.vanniktech.mavenPublish) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.kotlinJvm) apply false
}

subprojects {
    if (name in listOf("kreator-annotations", "kreator-compiler")) {
        group = "cz.petrchatrny.kreator"
        version = "0.0.2"
    }
}
