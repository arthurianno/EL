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
    buildTypes {
        create("debugDev")
        create("debugStage")
        create("releaseDev")
        create("releaseStage")
    }
}

dependencies {
    implementation(project(Module.core_adapter))
    implementation(project(Module.core_adapter_ktx))
    implementation(project(Module.core_collections))
    implementation(project(Module.core_common))
    implementation(project(Module.core_date))
    implementation(project(Module.core_font))
    implementation(project(Module.core_hardware))
    implementation(project(Module.core_intents))
    implementation(project(Module.core_interactor))
    implementation(project(Module.core_preferences))
    implementation(project(Module.core_resources))
    implementation(project(Module.core_rx))
    implementation(project(Module.core_rx_contacts))
    implementation(project(Module.core_rx_location))
    implementation(project(Module.core_security))
    implementation(project(Module.core_ui))
}
