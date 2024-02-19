plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}
android {

    compileSdk = AppConfig.completeSdk

    namespace = "com.elta.android.iiot"

    defaultConfig {
        minSdk = AppConfig.minSdk
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = AppConfig.javaVersion
        targetCompatibility = AppConfig.javaVersion
    }
    buildTypes {
        create("releaseDev")
        create("releaseStage")
    }
}

dependencies {
    implementation(Dependencies.Timber.core)
    implementation(Dependencies.IIOT.JacsonDatabind)

    implementation(files("libs/IIoT-SDK-0.2.0.aar"))
}
