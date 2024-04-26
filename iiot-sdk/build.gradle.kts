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
        create("huawei")
    }

}

dependencies {
    implementation(Dependencies.Timber.core)
    implementation(Dependencies.IIOT.JacsonDatabind)
    implementation(project(Module.common))
    implementation(project(Module.IoMT))

//    api(fileTree(baseDir = "libs"))
    compileOnly(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
}
