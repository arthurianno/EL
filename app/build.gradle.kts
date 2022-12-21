import org.jetbrains.kotlin.konan.properties.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("io.objectbox")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

val CREDENTIALS_DEBUG = "../keystore/credentials-debug.properties"
val CREDENTIALS_RELEASE = "../keystore/credentials-release.properties"
val STORE_FILE = "keystore.file"
val STORE_PASSWORD = "keystore.password"
val KEY_ALIAS = "key.alias"
val KEY_PASSWORD = "key.password"

fun getPropertiesFromFile(filename: String): Properties =
    Properties().apply {
        load(FileInputStream(file(filename)))
    }

android {
    compileSdk = AppConfig.completeSdk

    defaultConfig {
        applicationId = AppConfig.applicationId
        minSdk = AppConfig.minSdk
        targetSdk = AppConfig.targetSdk

        versionCode = Version.versionCode
        versionName = Version.versionName
        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {

        val credentialsDebug = getPropertiesFromFile(CREDENTIALS_DEBUG)

        getByName("debug") {
            storeFile = file(credentialsDebug[STORE_FILE].toString())
            storePassword = credentialsDebug[STORE_PASSWORD].toString()
            keyAlias = credentialsDebug[KEY_ALIAS].toString()
            keyPassword = credentialsDebug[KEY_PASSWORD].toString()
        }

        create("release") {
            var credentialsRelease = Properties()
            val credentialsReleaseFile = file(CREDENTIALS_RELEASE)
            if (credentialsReleaseFile.exists()) {
                credentialsRelease.load(FileInputStream(credentialsReleaseFile))
                println("credentials-release.properties is exist")
            } else {
                println("credentials-release.properties isn't exist - credentials-debug.properties will be used")
                credentialsRelease = credentialsDebug
            }

            storeFile = file(credentialsRelease[STORE_FILE].toString())
            storePassword = credentialsRelease[STORE_PASSWORD].toString()
            keyAlias = credentialsRelease[KEY_ALIAS].toString()
            keyPassword = credentialsRelease[KEY_PASSWORD].toString()
        }
    }

    buildTypes {

        all {
            resValue("string", "app_deep_link_host", AppConfig.DeppLink.host)
            resValue("string", "app_deep_link_schema", AppConfig.DeppLink.schema)
        }

        debug {
            buildConfigField("boolean", "IS_LOG_ENABLED", AppConfig.LogEnabled.debug.toString())
            signingConfig = signingConfigs["debug"]
        }

        release {
            buildConfigField("boolean", "IS_LOG_ENABLED", AppConfig.LogEnabled.release.toString())
            signingConfig = signingConfigs["release"]
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            proguardFiles.addAll(fileTree("proguard"))
        }
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

    splits.abi {
        isEnable = true
        reset()
        include("x86", "x86_64", "armeabi-v7a", "arm64-v8a")
        isUniversalApk = true
    }
    flavorDimensions += listOf("buildVariant")
    productFlavors {
        create("prod") {
            dimension = "buildVariant"
            buildConfigField("String", "SERVER_URL", "\"${AppConfig.ServerUrl.prod}\"")
        }
        create("stage") {
            dimension = "buildVariant"
            buildConfigField("String", "SERVER_URL", "\"${AppConfig.ServerUrl.stage}\"")
            versionNameSuffix = Version.NameSufix.stage
        }
        create("dev") {
            dimension = "buildVariant"
            buildConfigField("String", "SERVER_URL", "\"${AppConfig.ServerUrl.dev}\"")
            versionNameSuffix = Version.NameSufix.dev
        }
    }
}

kapt {
    correctErrorTypes = true
    javacOptions {
        option("-source", "8")
        option("-target", "8")
    }
}

dependencies {
    implementation(project(Module.core_all))
    implementation(project(Module.core_security))
    implementation(project(Module.core_rx))
    implementation(project(Module.core_resources))
    implementation(project(Module.core_adapter))
    implementation(project(Module.core_hardware))
    implementation(project(Module.core_preferences))
    implementation(project(Module.core_interactor))
    implementation(project(Module.common))
    api(project(Module.presentation))
    api(project(Module.data))

    implementation(Dependencies.Jetpack.reciclerView)
    implementation(Dependencies.Jetpack.multiDex)
    implementation(Dependencies.Yandex.mapKit)
    implementation(Dependencies.RxJava2.rxJava)
    implementation(Dependencies.RxJava2.rxPm)
    implementation(Dependencies.RxJava2.rxBluetooth)
    implementation(Dependencies.Cicerone.core)

    kapt(Dependencies.Dagger.daggerCompiler)
    kapt(Dependencies.Dagger.daggerAndroidProcessor)
    implementation(Dependencies.Dagger.dagger)
    implementation(Dependencies.Dagger.daggerAndroid)
    implementation(Dependencies.Dagger.daggerAndroidSupport)
    implementation(Dependencies.Timber.core)
    implementation(Dependencies.Utils.jsr310)
    implementation(Dependencies.Retrofit.core)
    implementation(Dependencies.Retrofit.gsonConverter)
    implementation(Dependencies.Retrofit.rxJava2Adapter)
    implementation(Dependencies.OkHttp.core)
    implementation(Dependencies.OkHttp.loggingInterceptor)
    implementation(Dependencies.Google.gson)
    implementation(platform(Dependencies.Google.FireBase.bom))
    implementation(Dependencies.Google.FireBase.messagingBom)
    implementation(Dependencies.Google.guavaConflictLost)
    implementation(Dependencies.Webim.core)

    testBaseDependencies()
}
