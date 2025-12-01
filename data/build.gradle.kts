plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("io.objectbox")
}

val version = getTagInfo()

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
        create("huawei") {
            buildConfigField("String", "APP_STORE", "\"${AppStore.HuaweiAppGallery.storeName}\"")
        }
        release {
            buildConfigField("String", "APP_STORE", "\"${AppStore.GooglePlay.storeName}\"")
        }
        debug {
            buildConfigField("String", "APP_STORE", "\"${AppStore.GooglePlay.storeName}\"")
        }
        all {
            buildConfigField("String", "VERSION_NAME", "\"${version}\"")
        }
    }
    namespace = "com.elta.android.data"
    kotlinOptions {
        freeCompilerArgs = listOf("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
    }
}

dependencies {

    implementation(project(Module.core_rx))
    implementation(project(Module.core_security))
    implementation(project(Module.core_hardware))
    implementation(project(Module.core_intents))
    implementation(project(Module.core_preferences))
    implementation(project(Module.common))
    api(project(Module.domain))
    api(project(Module.iiot_sdk))
    implementation(fileTree(baseDir = "libs"))

    implementation(Dependencies.Google.Services.fitness)
    implementation(Dependencies.Google.Services.auth)
    implementation(Dependencies.Google.Services.healthConnect)

    implementation(Dependencies.Google.FireBase.messagingBom)
    implementation(Dependencies.Google.FireBase.configBom)

    implementation(Dependencies.Kotlin.coroutinesCore)
    implementation(Dependencies.Kotlin.coroutinesRx2)

    implementation(Dependencies.RxJava2.rxKotlin)
    implementation(Dependencies.RxJava2.rxNetwork)
    implementation(Dependencies.RxJava2.rxReplaying)

    implementation(Dependencies.Dagger.javaxAnnotation)
    implementation(Dependencies.Dagger.javaxInject)
    implementation("androidx.room:room-common:2.7.2")
    implementation("androidx.room:room-runtime:2.7.2")
    implementation("androidx.room:room-common-jvm:2.7.2")
    kapt("androidx.room:room-compiler:2.7.2")
    implementation(Dependencies.Kotlin.metadataJvm)

    kapt(Dependencies.Dagger.daggerCompiler)
    kapt(Dependencies.Dagger.daggerAndroidProcessor)
    implementation(Dependencies.Dagger.dagger)
    implementation(Dependencies.Dagger.daggerAndroid)
    implementation(Dependencies.Dagger.daggerAndroidSupport)

    implementation(Dependencies.Timber.core)

    implementation(Dependencies.Retrofit.core)
    implementation(Dependencies.Retrofit.gsonConverter)
    implementation(Dependencies.Retrofit.rxJava2Adapter)
    implementation(Dependencies.OkHttp.core)
    implementation(Dependencies.OkHttp.loggingInterceptor)
    implementation(Dependencies.Google.gson)

    implementation(Dependencies.SocialNetworks.fb)
    implementation(Dependencies.SocialNetworks.vk)
    implementation(Dependencies.SocialNetworks.ok)

    implementation(Dependencies.ObjectBox.core)
    implementation(Dependencies.Utils.jsr310)

    implementation(Dependencies.Nordic.dfu)
    implementation(Dependencies.Nordic.ble)
    implementation(Dependencies.Nordic.bleKtx)

    implementation(Dependencies.Utils.essentials)

    implementation(Dependencies.Webim.core)

    implementation(Dependencies.Jetpack.Paging.pagingRuntime)
    implementation(Dependencies.Jetpack.Paging.rxPaging)

    testBaseDependencies()
}
