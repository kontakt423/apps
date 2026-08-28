plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// -----------------------------------------------------------------------------
// FIREBASE-FREE BY DESIGN
// The original Spotilol pulls in Firebase Analytics, Crashlytics and Performance
// via the `com.google.gms.google-services` plugin and a `google-services.json`.
// None of that is present here. The app builds and runs with zero Google/Firebase
// backend configuration. Crash reporting, if wanted later, is left to an opt-in
// OSS solution (e.g. ACRA) rather than Firebase.
// -----------------------------------------------------------------------------

android {
    namespace = "com.spotilol.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.spotilol.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        // Used by the update checker (GitHub Releases API). No Firebase Remote Config.
        // Points at THIS project's own repo so it checks your releases, not the
        // upstream original's. With no releases published yet, the check simply
        // finds nothing and no "update available" dialog is shown.
        buildConfigField("String", "UPDATE_REPO", "\"kontakt423/apps\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.webkit:webkit:1.11.0")

    // Media session / notification (AndroidX Media, not Firebase)
    implementation("androidx.media:media:1.7.0")

    // Coroutines for the update checker and background work
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Lightweight JSON parsing for GitHub Releases responses
    implementation("org.json:json:20240303")
}
