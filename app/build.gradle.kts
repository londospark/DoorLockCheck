import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// Release signing: values come from env vars (used by CI, see
// .github/workflows/release.yml) or from a local, gitignored
// keystore.properties file (see keystore.properties.example). If neither is
// present, the release build type falls back to debug signing so
// `assembleRelease`/`bundleRelease` always produce something installable -
// just not something Play Store will accept as a real release. See README
// "Release & Play Store submission" for setup instructions.
val keystoreProperties = Properties().apply {
    val propsFile = rootProject.file("keystore.properties")
    if (propsFile.exists()) {
        FileInputStream(propsFile).use { load(it) }
    }
}

fun releaseSigningValue(envName: String, propName: String): String? =
    System.getenv(envName) ?: keystoreProperties.getProperty(propName)

val releaseStoreFilePath = releaseSigningValue("RELEASE_KEYSTORE_PATH", "storeFile")
val releaseStorePassword = releaseSigningValue("RELEASE_KEYSTORE_PASSWORD", "storePassword")
val releaseKeyAlias = releaseSigningValue("RELEASE_KEY_ALIAS", "keyAlias")
val releaseKeyPassword = releaseSigningValue("RELEASE_KEY_PASSWORD", "keyPassword")

val hasReleaseSigning = listOf(
    releaseStoreFilePath,
    releaseStorePassword,
    releaseKeyAlias,
    releaseKeyPassword,
).all { !it.isNullOrBlank() }

if (!hasReleaseSigning) {
    logger.lifecycle(
        "No release signing config found (RELEASE_KEYSTORE_* env vars or " +
            "keystore.properties). The release build type will be signed with " +
            "the debug key - fine for sideloading on a watch, not for Play Store."
    )
}

// versionCode/versionName can be overridden by CI for tagged releases
// (-PreleaseVersionCode=N -PreleaseVersionName=X.Y.Z); local/manual builds
// keep the defaults below.
val releaseVersionCode = (findProperty("releaseVersionCode") as String?)?.toIntOrNull()
val releaseVersionName = findProperty("releaseVersionName") as String?

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
        versionCode = releaseVersionCode ?: 1
        versionName = releaseVersionName ?: "1.0"
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(releaseStoreFilePath!!)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            signingConfig = if (hasReleaseSigning) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
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
