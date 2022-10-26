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
val CI = "CI"
val LOCAL_PROPERTIES = "../local.properties"
val LOCAL_BUILD_FILE = "build.file"
val DEFAULT_BUILD_CONFIG_FILE = "../configuration-build-test.properties"
val CONFIG_ENVIRONMENT = "environment"
val CONFIG_SERVER_URL = "server.url"
val CONFIG_DEEP_LINK_HOST = "deep.link.host"
val CONFIG_APP_ID_SUFFIX = "app.id.suffix"
val CONFIG_APP_NAME_SUFFIX = "app.name.suffix"
val CONFIG_LOG_ENABLED = "log.enabled"

fun getPropertiesFromFile(filename: String): Properties =
    Properties().apply {
        load(FileInputStream(file(filename)))
    }

android {

    var buildConfigFile: String
    val ci = System.getenv(CI)
    if (ci != null) {
        println("Build runned on $ci CI server.")
        val configFilePropertyName =
            "${AppConfig.applicationId}.config.file".toUpperCase().replace(".", "_")
        buildConfigFile = System.getenv(configFilePropertyName)

        if (buildConfigFile.isNullOrEmpty()) {
            println("There is not environment variable $configFilePropertyName, default value will be used.")
            buildConfigFile = DEFAULT_BUILD_CONFIG_FILE
        }
    } else {
        println("Build runned on local machine.")
        val localProperties = getPropertiesFromFile(LOCAL_PROPERTIES)

        buildConfigFile = localProperties[LOCAL_BUILD_FILE]?.toString() ?: DEFAULT_BUILD_CONFIG_FILE

        if (buildConfigFile.isEmpty()) {
            println("There is property $LOCAL_BUILD_FILE at $LOCAL_PROPERTIES, default value will be used.")
            buildConfigFile = DEFAULT_BUILD_CONFIG_FILE
        }
    }
    println("Build config will be loaded from: $buildConfigFile.")
    val buildConfigProperties = getPropertiesFromFile(buildConfigFile)

    compileSdk = AppConfig.completeSdk

    defaultConfig {
        applicationId = AppConfig.applicationId
        minSdk = AppConfig.minSdk
        targetSdk = AppConfig.targetSdk

        versionCode = Releases.versionCode
        versionName = Releases.versionName

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
            buildConfigField(
                "String",
                "ENVIRONMENT",
                "\"${buildConfigProperties[CONFIG_ENVIRONMENT]}\""
            )
            buildConfigField(
                "String",
                "SERVER_URL",
                "\"${buildConfigProperties[CONFIG_SERVER_URL]}\""
            )
            buildConfigField(
                "boolean",
                "IS_LOG_ENABLED",
                buildConfigProperties[CONFIG_LOG_ENABLED].toString()
            )

            resValue(
                "string",
                "app_deep_link_host",
                buildConfigProperties[CONFIG_DEEP_LINK_HOST].toString()
            )
        }

        debug {
            buildConfigProperties[CONFIG_APP_ID_SUFFIX]?.let {
                applicationIdSuffix = it.toString()
            }

            buildConfigProperties[CONFIG_APP_NAME_SUFFIX]?.let {
                versionNameSuffix = it.toString()
            }
            signingConfig = signingConfigs["debug"]
        }

        release {
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

    testBaseDependencies()
}
