plugins {
    id("com.android.library")
    id("kotlin-android") // Замените kotlin("android") на id("kotlin-android") для консистентности
    id("kotlin-kapt")
    id("kotlin-parcelize")
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.0" // Добавьте эту строку
}

android {
    compileSdk = AppConfig.completeSdk

    val version = getTagInfo()

    defaultConfig {
        minSdk = AppConfig.minSdk
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        vectorDrawables.useSupportLibrary = true
        buildConfigField("boolean", "SHOW_LANGUAGE_SELECTION", "true")
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
        unitTests {
            all { it.jvmArgs("-noverify") }
            isIncludeAndroidResources = true
        }
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }
    buildTypes {
        release {
            buildConfigField("String", "APP_VERSION", "\"${version.versionName}\"")
            buildConfigField("String", "CLEAN_VERSION", "\"${version.versionName}\"")
            buildConfigField("String", "APP_STORE", "\"${AppStore.GooglePlay.storeName}\"")
            buildConfigField("String", "SERVER_URL", "\"${BackendVariant.prod.path}\"")
            buildConfigField("String", "BUILD_NUMBER", "\"${version.buildNumber}\"")
            buildConfigField("String", "BUILD_TYPE", "\"${version.buildType}\"")
            buildConfigField("String", "HOTFIX_VERSION", "\"${version.hotfixVersion}\"")
            // Fix 6: explicit false so intent is clear and searching the codebase is unambiguous.
            buildConfigField("boolean", "SHOW_LANGUAGE_SELECTION", "true")
        }
        debug {
            val debugVersionName = "\"${version.versionName}-debug(${version.buildNumber})\""
            buildConfigField("String", "APP_VERSION", debugVersionName)
            buildConfigField("String", "CLEAN_VERSION", "\"${version.versionName}\"")
            buildConfigField("String", "APP_STORE", "\"${AppStore.GooglePlay.storeName}\"")
            buildConfigField("boolean", "DEBUG", "true")
            buildConfigField("boolean", "SHOW_LANGUAGE_SELECTION", "true")
            buildConfigField("String", "SERVER_URL", "\"${BackendVariant.dev.path}\"")
            buildConfigField("String", "BUILD_NUMBER", "\"${version.buildNumber}\"")
            buildConfigField("String", "BUILD_TYPE", "\"${version.buildType}\"")
            buildConfigField("String", "HOTFIX_VERSION", "\"${version.hotfixVersion}\"")
        }
        create("huawei") {
            buildConfigField("String", "APP_VERSION", "\"${version.versionName}\"")
            buildConfigField("String", "CLEAN_VERSION", "\"${version.versionName}\"")
            buildConfigField("String", "APP_STORE", "\"${AppStore.HuaweiAppGallery.storeName}\"")
            buildConfigField("String", "SERVER_URL", "\"${BackendVariant.prod.path}\"")
            buildConfigField("String", "BUILD_NUMBER", "\"${version.buildNumber}\"")
            buildConfigField("String", "BUILD_TYPE", "\"${version.buildType}\"")
            buildConfigField("String", "HOTFIX_VERSION", "\"${version.hotfixVersion}\"")
            // Fix 6: explicit false so intent is clear and searching the codebase is unambiguous.
            buildConfigField("boolean", "SHOW_LANGUAGE_SELECTION", "true")
        }
    }
    namespace = "com.elta.android.presentation"
}

kapt {
    correctErrorTypes = true
}

configurations.all {
    resolutionStrategy.force("org.jetbrains.kotlin:kotlin-stdlib:${Dependencies.Kotlin.version}")
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

    implementation(platform(Dependencies.Jetpack.Compose.bom))
    implementation(Dependencies.Jetpack.Compose.bomMaterial)
    implementation(Dependencies.Jetpack.Compose.bomRxJava2)
    implementation(Dependencies.Jetpack.Compose.Accompanist.permissions)
    implementation(Dependencies.Coil.compose)
    implementation("androidx.glance:glance:1.1.1")
    implementation("androidx.glance:glance-preview:1.1.1")
    bomUiToolingDependencies()
    bomComposeTestsDependencies()
    implementation(Dependencies.Jetpack.Compose.activity)
    implementation(Dependencies.Jetpack.Compose.viewModel)

    implementation(Dependencies.Jetpack.core)
    implementation(Dependencies.Jetpack.fragment)
    implementation(Dependencies.Google.materialDesign)
    implementation(Dependencies.Google.Services.fitness)
    implementation(Dependencies.Google.Services.auth)
    implementation(Dependencies.Google.Services.healthConnect)
    implementation(Dependencies.Google.Services.MlKit.barcodeScaner)
    implementation(Dependencies.Google.Services.CameraX.core)
    implementation(Dependencies.Google.Services.CameraX.camera2)
    implementation(Dependencies.Google.Services.CameraX.lifecycle)
    implementation(Dependencies.Google.Services.CameraX.mlKit)
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
    implementation(Dependencies.Yandex.appMetrica)
    implementation(Dependencies.CustomView.pulseView)
    implementation(Dependencies.CustomView.datePicker)
    implementation(Dependencies.CustomView.lottie)
    debugImplementation("androidx.compose.ui:ui-tooling:1.9.0")

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
    implementation(Dependencies.Google.FireBase.crashlyticsBom)
    implementation(Dependencies.Google.FireBase.appMessagingBom)
    implementation(Dependencies.Google.FireBase.dynamicLinksBom)
    implementation(Dependencies.Google.GoogleMap.location)
    implementation(Dependencies.CustomView.cardView)
    implementation(Dependencies.Webim.core)
    implementation(Dependencies.OneSignal.core)

    implementation(Dependencies.Jetpack.Paging.pagingRuntime)
    implementation(Dependencies.Jetpack.Paging.pagingCompose)

    // Glance AppWidget
    implementation(Dependencies.Jetpack.Glance.appwidget)
    implementation(Dependencies.Jetpack.Glance.material3)
    implementation(Dependencies.Jetpack.Glance.preview)
    implementation(Dependencies.Jetpack.Glance.appwidgetPreview)
    implementation(Dependencies.Jetpack.WorkManager.core)

    testBaseDependencies()
    testImplementation(Dependencies.Test.kotlinJUnit)
    androidTestImplementation(Dependencies.Jetpack.WorkManager.test)
}
