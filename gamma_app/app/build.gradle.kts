plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.mca.gamma"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mca.gamma"
        minSdk = 29
        targetSdk = 35
        versionCode = 2
        versionName = "2.45"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("io.socket:socket.io-client:2.1.0")
    implementation("com.google.code.gson:gson:2.10")
    implementation("com.github.kittinunf.fuel:fuel:2.3.1")
    implementation("androidx.work:work-runtime:2.9.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.cardview:cardview:1.0.0")

}