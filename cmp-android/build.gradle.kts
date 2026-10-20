plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

android {
    namespace = "com.siddharth.apptemplate.android"
    compileSdk {
        version =
            release(
                libs.versions.android.compileSdk
                    .get()
                    .toInt(),
            ) { minorApiLevel = 1 }
    }

    defaultConfig {
        applicationId = "com.siddharth.apptemplate.android"
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
        targetSdk =
            libs.versions.android.targetSdk
                .get()
                .toInt()
        versionCode = 1
        versionName = "1.0.0"
    }
}

dependencies {
    implementation(project(":cmp-shared"))
    implementation(libs.androidx.activity.compose)
    implementation(compose.material3)
}
