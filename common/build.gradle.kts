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
        release {
            buildConfigField("String", "APP_VERSION", "\"${Version.versionName}\"")
        }
        debug {
            val debugVersionName = "\"${Version.versionName}${Version.prodNameSuffix}\""
            buildConfigField("String", "APP_VERSION", debugVersionName)
        }
        create("debugDev") {
            val debugVersionName = "\"${Version.versionName}${Version.devNameSuffix}\""
            buildConfigField("String", "APP_VERSION", debugVersionName)
        }
        create("debugStage") {
            val debugVersionName = "\"${Version.versionName}${Version.stageNameSuffix}\""
            buildConfigField("String", "APP_VERSION", debugVersionName)
        }
        create("releaseDev") {
            buildConfigField(
                "String",
                "APP_VERSION",
                "\"${Version.versionName}-${BackendVariant.dev.name}\""
            )
        }
        create("releaseStage") {
            buildConfigField(
                "String",
                "APP_VERSION",
                "\"${Version.versionName}-${BackendVariant.dev.name}\""
            )
        }
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
    implementation(Dependencies.Google.FireBase.storageBom)
    implementation(Dependencies.Jetpack.WorkManager.core)
}
