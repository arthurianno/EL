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
        targetSdk = AppConfig.targetSdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        vectorDrawables.useSupportLibrary = true

        buildConfigField("String", "APP_VERSION", "\"${Version.versionName}\"")
    }

    compileOptions {
        sourceCompatibility = AppConfig.javaVersion
        targetCompatibility = AppConfig.javaVersion
    }

    lint {
        quiet = true
        abortOnError = false
        ignoreWarnings = true
        disable.add("InvalidPackage")
        disable.add("OldTargetApi")
    }
    testOptions {
        unitTests.all {
            it.jvmArgs("-noverify")
        }
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = Dependencies.Jetpack.Compose.compilerVersion
    }
}

kapt {
    correctErrorTypes = true
    javacOptions {
        option("-source", "8")
        option("-target", "8")
    }
}

configurations.all {
    resolutionStrategy.force("org.jetbrains.kotlin:kotlin-stdlib:${Dependencies.kotlinVersion}")
}

dependencies {

    implementation(project(Module.core_all))
    implementation(project(Module.core_resources))
    implementation(project(Module.core_rx))
    implementation(project(Module.core_ui))
    implementation(project(Module.core_adapter))
    implementation(project(Module.core_adapter_ktx))
    implementation(project(Module.core_hardware))
    implementation(project(Module.core_rx_location))
    implementation(project(Module.core_collections))
    implementation(project(Module.core_intents))
    implementation(project(Module.core_interactor))
    implementation(project(Module.core_font))
    implementation(project(Module.core_date))
    implementation(project(Module.common))
    api(project(Module.domain))

    implementation(Dependencies.Kotlin.coroutinesCore)
    implementation(Dependencies.Kotlin.coroutinesRx2)

    implementation(Dependencies.Jetpack.Compose.material)
    implementation(Dependencies.Jetpack.Compose.foundation)
    implementation(Dependencies.Jetpack.Compose.ui)
    implementation(Dependencies.Jetpack.Compose.uiToolingPreview)
    implementation(Dependencies.Jetpack.Compose.activity)
    implementation(Dependencies.Jetpack.Compose.viewModel)
    implementation(Dependencies.Jetpack.Compose.rxJava2)

    implementation(Dependencies.Jetpack.core)
    implementation(Dependencies.Jetpack.fragment)
    implementation(Dependencies.Google.materialDesign)
    implementation(Dependencies.Google.Services.fitness)
    implementation(Dependencies.Google.Services.auth)
    implementation(Dependencies.CustomView.materialDialogs)
    implementation(Dependencies.RxJava2.rxKotlin)
    implementation(Dependencies.RxJava2.rxAndroid)
    implementation(Dependencies.RxJava2.rxPm)
    implementation(Dependencies.RxJava2.rxBinding)
    implementation(Dependencies.RxJava2.rxNetwork)
    implementation(Dependencies.RxJava2.rxPermissions)
    implementation(Dependencies.RxJava2.rxLocation)
    implementation(Dependencies.Dagger.javaxAnnotation)
    implementation(Dependencies.Dagger.javaxInject)
    implementation(Dependencies.Cicerone.core)
    implementation(Dependencies.CustomView.pinView)
    implementation(Dependencies.CustomView.expandableLayout)
    implementation(Dependencies.CustomView.tooltip)
    implementation(Dependencies.CustomView.materialEditText)
    implementation(Dependencies.Yandex.mapKit)
    implementation(Dependencies.Yandex.mapKitClustering)
    implementation(Dependencies.CustomView.pulseView)
    implementation(Dependencies.CustomView.datePicker)

    kapt(Dependencies.Dagger.daggerCompiler)
    kapt(Dependencies.Dagger.daggerAndroidProcessor)
    implementation(Dependencies.Dagger.dagger)
    implementation(Dependencies.Dagger.daggerAndroid)
    implementation(Dependencies.Dagger.daggerAndroidSupport)
    implementation(Dependencies.Timber.core)
    implementation(Dependencies.Utils.jsr310)
    implementation(Dependencies.Google.GoogleMap.maps)
    implementation(Dependencies.CustomView.inputMask)
    implementation(platform(Dependencies.Google.FireBase.bom))
    implementation(Dependencies.Google.FireBase.messagingBom)
    implementation(Dependencies.Google.FireBase.analyticsBom)
    implementation(Dependencies.Google.FireBase.appMessagingBom)
    implementation(Dependencies.Google.FireBase.dynamicLinksBom)
    implementation(Dependencies.Google.GoogleMap.location)
    implementation(Dependencies.CustomView.cardView)

    testComposeDependencies()
    testBaseDependencies()
}
