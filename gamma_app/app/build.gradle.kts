plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.mca.gamma"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.mca.gamma"
        minSdk = 29
        targetSdk = 37
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
    buildToolsVersion = "36.0.0"

}

dependencies {

    implementation("androidx.core:core-ktx:1.18.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.14.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("com.squareup.okhttp3:okhttp:5.3.2")
    implementation("androidx.security:security-crypto:1.1.0")
    implementation("io.socket:socket.io-client:2.1.2")
    implementation("com.google.code.gson:gson:2.14.0")
    implementation("com.github.kittinunf.fuel:fuel:2.3.1")
    implementation("androidx.work:work-runtime:2.11.2")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.cardview:cardview:1.0.0")

}