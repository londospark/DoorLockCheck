plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "dev.hubball.doorlockcheck"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "dev.hubball.doorlockcheck"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
    lint {
        abortOnError = false
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // BOM applied app-side too (not just androidTest): keeps main and test
    // classpaths resolving identical compose versions - see AGENTS.md.
    implementation(platform(libs.compose.bom))
    implementation(libs.activity.compose)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling)
    implementation(libs.core.splashscreen)
    implementation(libs.play.services.wearable)
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.wear.tooling.preview)
    implementation(libs.androidx.wear.watchface.complications)
    debugImplementation(libs.ui.test.manifest)
    debugImplementation(libs.ui.tooling)

    // Headless tests (Robolectric). All layers except "runs on a real watch" are
    // covered here - see AGENTS.md for why the androidTest layer was removed.
    testImplementation(libs.junit4core)
    testImplementation(libs.robolectriclib)
    testImplementation(libs.androidxtestcore)
    testImplementation(libs.turbinelib)
    testImplementation(libs.coroutinestestlib)
    testImplementation(platform(libs.compose.bom))
    testImplementation(libs.ui.test.junit4)
    testImplementation(libs.espressocorelib)
}
