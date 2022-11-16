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
    implementation(project(Module.core_interactor))

    implementation(Dependencies.Kotlin.coroutinesCore)
    implementation(Dependencies.RxJava2.rxKotlin)
    implementation(Dependencies.Dagger.javaxAnnotation)
    implementation(Dependencies.Dagger.javaxInject)
    implementation(Dependencies.Timber.core)
    implementation(Dependencies.Utils.jsr310)
    implementation(platform(Dependencies.Google.FireBase.bom))
    implementation(Dependencies.Google.FireBase.databaseBom)
}
