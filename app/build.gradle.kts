plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.btqt_3"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.btqt_3"
        minSdk = 29
        targetSdk = 34
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

    androidResources {
        noCompress("tflite", "task")
    }
}

dependencies {
    // Thư viện giao diện
    implementation("androidx.cardview:cardview:1.0.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // --- THƯ VIỆN CHO NHẬN DIỆN CỬ CHỈ TAY (AI) ---
    // MediaPipe Tasks Vision (AI Core)
    implementation("com.google.mediapipe:tasks-vision:0.10.0")

    // THỐNG NHẤT TẤT CẢ CAMERA X VỀ 1.3.0
    val cameraxVersion = "1.3.0"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")

    // Kiểm thử
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}