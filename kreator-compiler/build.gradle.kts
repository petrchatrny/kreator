import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.vanniktech.mavenPublish)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":kreator-annotations")) // Annotations
    implementation(libs.ksp.api) // KSP
    implementation(libs.kotlinpoet) // Kotlin Poet
    implementation(libs.kotlinpoet.ksp) // Kotlin Poet KSP plugin

    testImplementation(libs.kotlin.test) // for regular Kotlin tests
    testImplementation(libs.kctfork.ksp)
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates(group.toString(), "kreator-compiler", version.toString())

    pom {
        name = "Kreator Compiler"
        description = "KSP code generation processor module for Kreator - DTO generation library."
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
