// Root build.gradle.kts
// Project-wide configuration and repositories

plugins {
    // Android Gradle Plugin - single source of truth for all modules
    id("com.android.application") version "8.1.0" apply false

    // Kotlin support
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false

    // Dependency updates plugin (optional, for future use)
    id("com.github.ben-manes.versions") version "0.48.0" apply false
}

// All projects configuration
allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

// Subprojects configuration
// subprojects configuration removed to fix Unresolved reference errors
// The app module is self-configured in app/build.gradle.kts
// subprojects {
//     plugins.withId("com.android.application") {
//         configure<com.android.build.gradle.AppExtension> {
//             compileSdk = 34
//
//             defaultConfig {
//                 minSdk = 21
//                 targetSdk = 34
//                 versionCode = 9012
//                 versionName = "9.0.12"
//
//                 testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//             }
//
//             compileOptions {
//                 sourceCompatibility = JavaVersion.VERSION_17
//                 targetCompatibility = JavaVersion.VERSION_17
//             }
//
//             kotlinOptions {
//                 jvmTarget = "17"
//             }
//         }
//     }
// }

// Task to print dependency tree (useful for debugging)
tasks.register("dependencyTree") {
    doLast {
        allprojects.forEach { project ->
            println("${project.name}:")
            project.configurations.forEach { config ->
                if (config.isCanBeResolved) {
                    try {
                        val files = config.resolvedConfiguration.resolvedArtifacts
                        if (files.isNotEmpty()) {
                            println("  ${config.name}:")
                            files.forEach { artifact ->
                                println("    ${artifact.moduleVersion.id}")
                            }
                        }
                    } catch (e: Exception) {
                        // Skip configurations that can't be resolved
                    }
                }
            }
        }
    }
}
