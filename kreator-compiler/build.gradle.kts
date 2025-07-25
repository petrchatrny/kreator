plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

group = "cz.petrchatrny.kreator"
version = "0.0.1"

kotlin {
    jvm()
    sourceSets {
        val jvmMain by getting {
            kotlin.srcDir("src/main/kotlin")
            resources.srcDir("src/main/resources")

            dependencies {
                implementation(project(":kreator-annotations")) // Annotations
                implementation(libs.ksp.api) // KSP
                implementation(libs.kotlinpoet) // Kotlin Poet
                implementation(libs.kotlinpoet.ksp) // Kotlin Poet KSP plugin
            }
        }
    }
}

//mavenPublishing {
//    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
//    signAllPublications()
//
//    coordinates(group.toString(), "kreator-annotations", version.toString())
//
//    pom {
//        name = "Kreator Compiler"
//        description = "KSP code generation processor module for Kreator code generation library."
//        inceptionYear = "2025"
//        url = "https://github.com/petrchatrny/kreator"
//        licenses {
//            license {
//                name = "MIT License"
//                url = "https://opensource.org/licenses/MIT"
//                distribution = "repo"
//            }
//        }
//        developers {
//            developer {
//                id = "petrchatrny"
//                name = "Petr Chatrný"
//                email = "posta@petrchatrny.cz"
//            }
//        }
//        scm {
//            url = "https://github.com/petrchatrny/kreator"
//            connection = "scm:git:https://github.com/petrchatrny/kreator.git"
//            developerConnection = "scm:git:git@github.com:petrchatrny/kreator.git"
//        }
//    }
//}
