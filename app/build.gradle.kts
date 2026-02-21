plugins {
    alias(libs.plugins.android.application)

}

android {
    namespace = "com.example.carapplication"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.carapplication"
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
        viewBinding = true
    }

}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.room.common.jvm)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("androidx.room:room-runtime:2.8.4")
    annotationProcessor("androidx.room:room-compiler:2.8.4")

    implementation ("androidx.lifecycle:lifecycle-viewmodel:2.10.0")
    implementation ("androidx.lifecycle:lifecycle-livedata:2.10.0")

    implementation ("com.google.code.gson:gson:2.11.0")

    implementation("com.github.bumptech.glide:glide:5.0.5")
    implementation("de.hdodenhof:circleimageview:3.1.0")
}