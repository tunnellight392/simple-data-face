plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.tunnellight.simpledataface"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.tunnellight.simpledataface"
        // Watch Face Format v2 requires Wear OS 5 (API 34+).
        minSdk = 34
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
}
