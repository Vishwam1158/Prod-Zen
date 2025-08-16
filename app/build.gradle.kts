//plugins {
//    alias(libs.plugins.android.application)
//    alias(libs.plugins.jetbrains.kotlin.android)
//    alias(libs.plugins.dagzer.hilt.android) apply false
//    alias(libs.plugins.kotlin.kapt) apply false
//}

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // Added for Hilt (dependency injection) and Room (database)
//    id("kotlin-kapt")
    id("org.jetbrains.kotlin.kapt")
    id("com.google.dagger.hilt.android")  // Use the latest version
}


android {
    namespace = "com.viz.prodzen"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.viz.prodzen"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        // Java 17 is required by Hilt and other modern libraries
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17" // for hilt and mordern libraries
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core & UI
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    testImplementation("junit:junit:4.13.2")


    // Navigation (Required for navigating between screens)
    implementation("androidx.navigation:navigation-compose:2.7.5")

    // ViewModel (Required for MVVM architecture)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

    // Hilt (Dependency Injection - Required for the entire app)
    implementation("com.google.dagger:hilt-android:2.48.1")
    kapt("com.google.dagger:hilt-compiler:2.48.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Room (Local Database - Required for saving app settings)
    implementation("androidx.room:room-runtime:2.6.0")
    kapt("androidx.room:room-compiler:2.6.0")
    implementation("androidx.room:room-ktx:2.6.0")

    // Coil (Required for loading app icons asynchronously)
    implementation("io.coil-kt:coil-compose:2.5.0")
}