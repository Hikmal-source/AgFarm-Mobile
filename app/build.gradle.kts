plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.hikmal.agfarm"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.hikmal.agfarm"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
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
    buildFeatures {
        compose = true
    }
}

dependencies {
    val firebaseBom = platform(libs.firebase.bom)
    implementation(firebaseBom)

    // Firebase Realtime Database - untuk sinkronisasi data real-time
    implementation(libs.firebase.database.ktx)

    // Firebase Authentication - untuk login/registrasi dengan Email/Password
    implementation(libs.firebase.auth.ktx)

    // Coroutines untuk Android - manajemen thread untuk operasi asinkron
    implementation(libs.kotlinx.coroutines.android)

    // Play Services extensions - mengubah Firebase task menjadi suspend function
    // Memungkinkan penggunaan .await() pada Firebase operations
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.androidx.navigation.compose)
    implementation("androidx.biometric:biometric-ktx:1.2.0-alpha05")

    implementation ("androidx.security:security-crypto:1.0.0")

    
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.coil.compose)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}