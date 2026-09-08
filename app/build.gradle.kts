plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "dev.jvqtil.cuber"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "dev.jvqtil.cuber"
        minSdk = 26
        targetSdk = 37
        versionCode = 130
        versionName = "1.3.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = (
                    providers.gradleProperty("CUBER_KEYSTORE").orNull
                        ?: System.getenv("CUBER_KEYSTORE")
                    )?.let { rootProject.file(it) }

            storePassword =
                providers.gradleProperty("CUBER_KEYSTORE_PASSWORD").orNull
                    ?: System.getenv("CUBER_KEYSTORE_PASSWORD")

            keyAlias =
                providers.gradleProperty("CUBER_KEY_ALIAS").orNull
                    ?: System.getenv("CUBER_KEY_ALIAS")

            keyPassword =
                providers.gradleProperty("CUBER_KEY_PASSWORD").orNull
                    ?: System.getenv("CUBER_KEY_PASSWORD")
        }
    }


    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }

        release {
            signingConfig = signingConfigs.getByName("release")

            optimization {
                enable = true
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.lib.scrambles)
    implementation(libs.androidsvg.aar)
    implementation(libs.androidx.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
}