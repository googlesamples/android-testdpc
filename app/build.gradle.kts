// app/build.gradle.kts
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.afwsamples.testdpc"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.afwsamples.testdpc"
        minSdk = 21
        targetSdk = 34
        versionCode = 9012
        versionName = "9.0.12"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        buildConfig = true
        aidl = true
    }

    // AIDL configuration
    aaptOptions {
        ignoreAssetsPattern = "!.svn:!.git:!.ds_store:!*.scc:.*:<dir>_:!CVS:!thumbs.db:!picasa.ini:!*~"
    }

    // Resource configuration
    // resourceConfigurations += setOf("en", "hdpi", "xhdpi", "xxhdpi", "xxxhdpi")

    sourceSets {
        getByName("main") {
            manifest.srcFile("../src/main/AndroidManifest.xml")
            java.srcDirs("../src/main/java")
            res.srcDirs("../src/main/res")
            aidl.srcDirs("../src/main/aidl")
        }
        getByName("debug") {
            manifest.srcFile("../src/main/AndroidManifestDebug.xml")
        }
    }
}

dependencies {
    // AndroidX Core
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core:1.9.0")
    implementation("androidx.core:core-ktx:1.9.0")

    // AndroidX Fragment
    implementation("androidx.fragment:fragment:1.5.5")
    implementation("androidx.fragment:fragment-ktx:1.5.5")

    // AndroidX Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime:2.5.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.5.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-process:2.5.1")

    // AndroidX Preference
    implementation("androidx.preference:preference:1.2.1")
    implementation("androidx.preference:preference-ktx:1.2.1")

    // AndroidX RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.2.1")

    // AndroidX Legacy
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.legacy:legacy-support-v13:1.0.0")

    // AndroidX Window
    implementation("androidx.window:window:1.2.0")
    implementation("androidx.window:window-core:1.2.0")

    // AndroidX Room (for database)
    implementation("androidx.room:room-runtime:2.5.2")
    implementation("androidx.room:room-ktx:2.5.2")
    annotationProcessor("androidx.room:room-compiler:2.5.2")

    // AndroidX SQLite
    implementation("androidx.sqlite:sqlite:2.3.1")
    implementation("androidx.sqlite:sqlite-framework:2.3.1")

    // Material Design
    implementation("com.google.android.material:material:1.11.0")

    // AndroidX Enterprise
    implementation("androidx.enterprise:enterprise-feedback:1.1.0")

    // Constraint Layout
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Setup Wizard
    implementation(project(":setupdesign"))
    implementation(project(":setupcompat"))

    // Google Guava (from Bazel setup)
    implementation("com.google.guava:guava:31.1-android")

    // Bouncy Castle
    implementation("org.bouncycastle:bcprov-jdk15on:1.70")
    implementation("org.bouncycastle:bcpkix-jdk15on:1.70")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("androidx.test.ext:junit:1.1.5")
    testImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Optional: AIDL support
    // implementation("androidx.aidl:aidl:1.0.0")
}
