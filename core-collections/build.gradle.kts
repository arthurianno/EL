plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}

android {

    compileSdk = AppConfig.completeSdk

    defaultConfig {
        minSdk = AppConfig.minSdk
        targetSdk = AppConfig.targetSdk

        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
        }
    }

    lint {
        abortOnError = false
    }
}

configurations.all {
    resolutionStrategy.force("org.jetbrains.kotlin:kotlin-stdlib:${Dependencies.kotlinVersion}")
}

kapt {
    correctErrorTypes = true
}

dependencies {
}
