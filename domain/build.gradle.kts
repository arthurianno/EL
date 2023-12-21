plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
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

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
    buildTypes {
        create("releaseDev")
        create("releaseStage")
    }
}

dependencies {
    implementation(project(Module.core_interactor))
    implementation(project(Module.core_rx))
    implementation(project(Module.common))

    implementation(Dependencies.Kotlin.coroutinesRx2)
    implementation(Dependencies.Kotlin.coroutinesCore)
    implementation(Dependencies.RxJava2.rxKotlin)

    implementation(Dependencies.Dagger.javaxAnnotation)
    implementation(Dependencies.Dagger.javaxInject)
    kapt(Dependencies.Dagger.daggerCompiler)
    kapt(Dependencies.Dagger.daggerAndroidProcessor)
    implementation(Dependencies.Dagger.dagger)
    implementation(Dependencies.Dagger.daggerAndroid)
    implementation(Dependencies.Dagger.daggerAndroidSupport)

    implementation(Dependencies.Timber.core)
    implementation(Dependencies.Utils.jsr310)
    implementation(Dependencies.Jetpack.Paging.pagingRuntime)

    testBaseDependencies()
    testImplementation(Dependencies.Test.jsr310) {
        exclude(module = Dependencies.Utils.jsr310)
    }
}
