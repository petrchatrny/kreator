@file:OptIn(ExperimentalWasmDsl::class)

import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.vanniktech.mavenPublish)
}

group = "cz.petrchatrny.kreator"
version = "0.0.1"

kotlin {
    jvm()
    androidTarget {
        publishLibraryVariants("release")
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    linuxX64()
    js()
    wasmJs()

    sourceSets {
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

android {
    namespace = "cz.petrchatrny.kreator"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates(group.toString(), "kreator-annotations", version.toString())

    pom {
        name = "Kreator Annotations"
        description = "Annotations module for the Kreator - DTO generation library."
        inceptionYear = "2025"
        url = "https://github.com/petrchatrny/kreator"
        licenses {
            license {
                name = "MIT License"
                url = "https://opensource.org/licenses/MIT"
                distribution = "repo"
            }
        }
        developers {
            developer {
                id = "petrchatrny"
                name = "Petr Chatrný"
                email = "posta@petrchatrny.cz"
            }
        }
        scm {
            url = "https://github.com/petrchatrny/kreator"
            connection = "scm:git:https://github.com/petrchatrny/kreator.git"
            developerConnection = "scm:git:git@github.com:petrchatrny/kreator.git"
        }
    }
}
