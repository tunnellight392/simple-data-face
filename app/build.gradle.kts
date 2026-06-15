import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

// This is a resource-only Watch Face Format face (hasCode=false). A watch face with
// minSdk >= 33 must contain NO dex files, so drop the Kotlin stdlib that AGP adds by
// default — otherwise bundletool rejects the .aab ("cannot have dex files").
configurations.configureEach {
    if (name.endsWith("RuntimeClasspath")) {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
    }
}

// Load signing credentials from keystore.properties (kept out of source control).
val keystoreProperties = Properties()
val keystorePropertiesFile = rootProject.file("keystore.properties")
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    namespace = "com.tunnellight.simpledataface"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.tunnellight.simpledataface"
        // Watch Face Format v2 requires Wear OS 5 (API 34+).
        minSdk = 34
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
}

// AGP still emits an empty classes.dex even with no code; bundletool rejects ANY dex
// in a watch face (minSdk >= 33), so delete the merged dex before the APK/AAB are
// packaged. The face is hasCode=false, so it needs no dex at all.
val mergedReleaseDexDir: File =
    layout.buildDirectory.dir("intermediates/dex/release/mergeDexRelease").get().asFile
tasks.matching { it.name == "mergeDexRelease" }.configureEach {
    doLast {
        mergedReleaseDexDir.walkTopDown()
            .filter { it.isFile && it.extension == "dex" }
            .forEach { it.delete() }
    }
}
