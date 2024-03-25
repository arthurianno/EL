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
    namespace = "com.nullgr.core.intents"
}

dependencies {

    implementation(project(Module.core_rx))

    implementation(Dependencies.Jetpack.annotations)
    implementation(Dependencies.Jetpack.browser)
    implementation(Dependencies.Jetpack.appCompat)
    implementation(Dependencies.RxJava2.rxKotlin)
}
