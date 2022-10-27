plugins {
    id("com.android.library")
    kotlin("android")
}

android {

    compileSdk = AppConfig.completeSdk

    defaultConfig {
        minSdk = AppConfig.minSdk
        targetSdk = AppConfig.targetSdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = AppConfig.javaVersion
        targetCompatibility = AppConfig.javaVersion
    }
}

dependencies {
    implementation(project(Module.core_common))
    implementation(project(Module.core_font))

    compileOnly(Dependencies.Jetpack.appCompat)
    compileOnly(Dependencies.Jetpack.reciclerView)
}
