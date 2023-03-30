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

    buildFeatures {
        viewBinding = true
    }
    buildTypes {
        create("debugDev")
        create("debugStage")
        create("releaseDev")
        create("releaseStage")
    }
}

dependencies {
    implementation(project(Module.core_rx))
    implementation(project(Module.core_adapter))

    compileOnly(Dependencies.Jetpack.reciclerView)

    implementation(Dependencies.RxJava2.rxKotlin)
    implementation(Dependencies.RxJava2.rxAndroid)
}
