# Installation

The Kreator library is delivered in two main modules.
Both of these modules are deployed to the Maven Central repository.
The first module `kreator-annotations` contains annotation definitions and is delivered as KMP compatible, therefore it has an artifact for
each platform separately.
The second module `kreator-compiler` contains an annotation processor for code generation.
The KSP tool is used for annotation processing and the Kotlin-Poet library is used for code generation.

## Gradle repositories

Add the following code to your `settings.gradle.kts`.

<code-block lang="kotlin" title="settings.gradle.kts">
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
</code-block>

## KSP plugin

Adding the plugin in the `build.gradle` file.

<tabs>
    <tab id="ksp-plugin-kotlin" title="Kotlin">
        <code-block lang="Kotlin">
            plugins {
                kotlin("jvm") version "2.1.10"
                id("com.google.devtools.ksp") version "2.1.10-1.0.29"
            }
        </code-block>
    </tab>
    <tab id="ksp-plugin-groovy" title="Groovy">
        <code-block lang="Groovy">
            plugins {
                id 'org.jetbrains.kotlin.jvm' version '2.1.10'
                id 'com.google.devtools.ksp' version '2.1.10-1.0.29'
            }
        </code-block>
    </tab>
    <tab id="ksp-plugin-vc" title="Version catalog">

```toml
[versions]
kotlin = "2.1.10"
ksp = "2.1.10-1.0.29"

[plugins]
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

<br>

```kotlin
plugins {  
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ksp)
}
```

</tab>
</tabs>

## Setup for standard Kotlin application

Using the library in the `build.gradle` file.

<tabs>
    <tab id="kt-app-kotlin" title="Kotlin">
        <code-block lang="Kotlin">
            dependencies {
                implementation("cz.petrchatrny.kreator.kreator-annotations:0.0.3")
                ksp("cz.petrchatrny.kreator:kreator‐compiler:0.0.3")
            }
        </code-block>
    </tab>
    <tab id="kt-app-groovy" title="Groovy">
        <code-block lang="Groovy">
            dependencies {
                implementation 'cz.petrchatrny.kreator:kreator-annotations:0.0.3'
                ksp 'cz.petrchatrny.kreator:kreator-compiler:0.0.3'
            }
        </code-block>
    </tab>
    <tab id="kt-app-vc" title="Version catalog">

```toml
[versions]
kreator = "0.0.3"

[libraries]
kreator-annotations = { module = "cz.petrchatrny.kreator:kreator-annotations", version.ref = "kreator" }
kreator-compiler = { module = "cz.petrchatrny.kreator:kreator-compiler", version.ref = "kreator" }
```

<br>

```kotlin
dependencies {
    implementation(libs.kreator.annotations)
    ksp(libs.kreator.compiler)
}
```

</tab>
</tabs>

## Setup for KMP application

As stated in the official Kotlin documentation, the use of the KSP tool in a KMP project must be defined separately for each target.

<tabs>
    <tab id="kmp-app-kotlin" title="Kotlin">
        <code-block lang="Kotlin">
            sourceSets {
                commonMain {
                    dependencies {
                        implementation("cz.petrchatrny.kreator.annotations:0.0.3")
                    }
                }
            }
            dependencies {
                add("kspCommonMainMetadata", "cz.petrchatrny.kreator:compiler:0.0.3")
                add("kspAndroid", "cz.petrchatrny.kreator.compiler:0.0.3")
                add("kspIosX64", "cz.petrchatrny.kreator.compiler:0.0.3")
                add("kspIosArm64", "cz.petrchatrny.kreator.compiler:0.0.3")
                // other targets...
            }
        </code-block>
    </tab>
    <tab id="kmp-app-groovy" title="Groovy">
        <code-block lang="Groovy">
            sourceSets {
                commonMain {
                    dependencies {
                        implementation 'cz.petrchatrny.kreator.annotations:annotations:0.0.3'
                    }
                }
            }
            dependencies {
                add 'kspCommonMainMetadata', 'cz.petrchatrny.kreator:compiler:0.0.3'
                add 'kspAndroid', 'cz.petrchatrny.kreator:compiler:0.0.3'
                add 'kspIosX64', 'cz.petrchatrny.kreator:compiler:0.0.3'
                add 'kspIosArm64', 'cz.petrchatrny.kreator:compiler:0.0.3'
                // other targets...
            }
        </code-block>
    </tab>
    <tab id="kmp-app-vc" title="Version catalog">

```toml
[versions]
kreator = "0.0.3"

[libraries]
kreator-annotations = { module = "cz.petrchatrny.kreator:kreator-annotations", version.ref = "kreator" }
kreator-compiler = { module = "cz.petrchatrny.kreator:kreator-compiler", version.ref = "kreator" }
```

<br>

```kotlin
sourceSets {
    commonMain {
        dependencies {
            implementation(libs.kreator.annotations)
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", libs.kreator.compiler)
    add("kspAndroid", libs.kreator.compiler)
    add("kspIosX64", libs.kreator.compiler)
    add("kspIosArm64", libs.kreator.compiler)
    // other targets...
}
```

</tab>
</tabs>
