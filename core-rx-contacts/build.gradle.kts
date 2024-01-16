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
    namespace = "com.nullgr.core.rx.contacts"
}

dependencies {

    implementation(project(Module.core_collections))
    implementation(project(Module.core_rx))

    implementation(Dependencies.Jetpack.core)

    implementation(Dependencies.RxJava2.rxKotlin)
    implementation(Dependencies.RxJava2.rxAndroid)
    implementation(Dependencies.RxJava2.rxRelay)

    implementation(Dependencies.Test.rules)
    testImplementation(Dependencies.Test.rules)
    testBaseDependencies()
}
