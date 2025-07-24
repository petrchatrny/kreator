plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

group = "cz.petrchatrny"
version = "0.0.1"

kotlin {
    jvm()
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(libs.ksp.api) // KSP
                implementation(libs.kotlinpoet) // Kotlin Poet
                implementation(libs.kotlinpoet.ksp) // Kotlin Poet KSP plugin
            }
        }
    }
}
