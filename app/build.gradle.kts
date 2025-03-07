plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    packaging {
        resources {
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/DEPENDENCIES"
        }
    }
    namespace = "com.example.saahas"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.saahas"
        minSdk = 24
        targetSdk = 35
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation ("ai.picovoice:porcupine-android:3.0.0")

    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.11.0")
    wearApp(":Wear")
    implementation ("com.google.maps:google-maps-services:2.2.0")
    //wear os
    implementation ("com.google.android.gms:play-services-wearable:18.0.0")
    implementation ("com.google.maps:google-maps-services:2.0.0")
    implementation ("com.google.maps.android:android-maps-utils:3.0.0")
    implementation ("org.slf4j:slf4j-simple:2.0.9")
    val room_version = "2.6.1"

    implementation("androidx.room:room-runtime:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    //mEDIA
    implementation("androidx.media:media:1.6.0")
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    // Hilt Navigation
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")
    //Hilt DI
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-android-compiler:2.51.1")

    //Life Cycle
    val lifecycle_version = "2.8.7"
// ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
// ViewModel utilities for Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycle_version")
    implementation("androidx.compose.ui:ui:1.7.4")
    implementation("androidx.compose.material:material:1.7.4")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.4")
    implementation("androidx.navigation:navigation-compose:2.7.5")
    implementation("androidx.compose.ui:ui-text-google-fonts:1.5.4")
    //Live data
    implementation("androidx.compose.runtime:runtime-livedata:1.7.4")
    //Google sign credential manager
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    //Speech
    implementation ("com.google.cloud:google-cloud-speech:4.51.0")
    implementation ("com.google.auth:google-auth-library-oauth2-http:1.17.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}