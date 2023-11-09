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
        create("debugDev")
        create("debugStage")
        create("releaseDev")
        create("releaseStage")
    }
}

dependencies {
    implementation(Dependencies.Timber.core)

//    api(fileTree(baseDir = "libs"))
    compileOnly(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
}
