plugins {
    id("com.android.library")
    kotlin("android")
}

android {

    compileSdk = AppConfig.completeSdk

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
    namespace = "com.nullgr.core.rx.location"
}

dependencies {

    implementation(project(Module.core_rx))
    implementation(project(Module.core_intents))

    implementation(Dependencies.RxJava2.rxKotlin)
    implementation(Dependencies.RxJava2.rxAndroid)
    implementation(Dependencies.RxJava2.rxRelay)
    implementation(Dependencies.RxJava2.rxLocation)

    compileOnly(Dependencies.Google.GoogleMap.location)
}
