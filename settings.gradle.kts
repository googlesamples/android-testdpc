// settings.gradle.kts
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()

        // For Gradle Plugins
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }

    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "com.android.application" -> useModule("com.android.tools.build:gradle:8.1.0")
                "org.jetbrains.kotlin.android" -> useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0")
                "com.github.ben-manes.versions" -> useModule("com.github.ben-manes:gradle-versions-plugin:0.48.0")
            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "testdpc"
include(":app")
include(":setupdesign")
include(":setupcompat")
