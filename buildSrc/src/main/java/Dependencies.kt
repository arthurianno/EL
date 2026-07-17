@file:Suppress("UnstableApiUsage")

object Dependencies {
    const val gradleVersion = "8.7.0"
    const val ktLintVersion = "10.3.0"
    const val dependenciesUpdateVersion = "0.42.0"
    const val detektGradlePluginVersion = "1.22.0-RC2"

    object Kotlin {
        const val version = "2.2.0"
        private const val serializationVersion = "1.4.0"
        private const val coroutinesVersion = "1.6.4"
        private const val dateTimeVersion = "0.3.1"
        private const val metadataVersion = "0.9.0"

        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib:$version"
        const val coroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion"
        const val coroutinesRx2 = "org.jetbrains.kotlinx:kotlinx-coroutines-rx2:$coroutinesVersion"
        const val serialization = "org.jetbrains.kotlinx:kotlinx-serialization-core:$serializationVersion"
        const val serializationJson = "org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion"
        const val dateTime = "org.jetbrains.kotlinx:kotlinx-datetime:$dateTimeVersion"
        const val metadataJvm = "org.jetbrains.kotlinx:kotlinx-metadata-jvm:$metadataVersion"
    }

    object Jetpack {
        private const val coreVersion = "1.8.0"
        private const val fragmentVersion = "1.4.1"
        private const val lifeCycleVersion = "2.5.0"
        private const val constraintLayoutVersion = "2.1.4"
        private const val appcompatVersion = "1.4.2"
        private const val mutlidexVersion = "2.0.1"
        private const val recyclerViewVersion = "1.2.1"
        private const val annotationsVersion = "1.5.0"
        private const val browserVersion = "1.4.0"
        private const val pagingVersion = "3.2.0"

        object Paging {
            const val pagingRuntime = "androidx.paging:paging-runtime:$pagingVersion"
            const val pagingCompose = "androidx.paging:paging-compose:$pagingVersion"
            const val rxPaging = "androidx.paging:paging-rxjava2:$pagingVersion"
        }

        const val core = "androidx.core:core-ktx:$coreVersion"
        const val fragment = "androidx.fragment:fragment-ktx:$fragmentVersion"
        const val viewModel = "androidx.lifecycle:lifecycle-viewmodel-ktx:$lifeCycleVersion"
        const val lifeCycle = "androidx.lifecycle:lifecycle-runtime-ktx:$lifeCycleVersion"
        const val appCompat = "androidx.appcompat:appcompat:$appcompatVersion"
        const val constraintLayout = "androidx.constraintlayout:constraintlayout:$constraintLayoutVersion"
        const val multiDex = "androidx.multidex:multidex:$mutlidexVersion"
        const val reciclerView = "androidx.recyclerview:recyclerview:$recyclerViewVersion"
        const val annotations = "androidx.annotation:annotation:$annotationsVersion"
        const val browser = "androidx.browser:browser:$browserVersion"

        object WorkManager {
            private const val version = "2.8.1"

            const val core = "androidx.work:work-runtime-ktx:$version"
            const val rxJava2 = "androidx.work:work-rxjava2:$version"
            const val test = "androidx.work:work-testing:$version"
        }

        object Glance {
            private const val version = "1.1.0"
            const val appwidget = "androidx.glance:glance-appwidget:$version"
            const val material3 = "androidx.glance:glance-material3:$version"
            const val preview = "androidx.glance:glance-preview:$version"
            const val appwidgetPreview = "androidx.glance:glance-appwidget-preview:$version"
        }

        object Compose {
            const val compilerVersion = "2.2.0"
            private const val bomVersion = "2024.09.01"
            private const val activityVersion = "1.9.2"

            const val bom = "androidx.compose:compose-bom:$bomVersion"
            const val bomMaterial = "androidx.compose.material:material"
            const val bomFoundation = "androidx.compose.foundation:foundation"
            const val bomUiTooling = "androidx.compose.ui:ui-tooling"
            const val bomUiToolingPreview = "androidx.compose.ui:ui-tooling-preview"
            const val bomRxJava2 = "androidx.compose.runtime:runtime-rxjava2"
            const val activity = "androidx.activity:activity-compose:$activityVersion"
            const val viewModel = "androidx.lifecycle:lifecycle-viewmodel-compose:$lifeCycleVersion"

            object Accompanist {
                private const val version = "0.30.0"

                const val permissions = "com.google.accompanist:accompanist-permissions:$version"
            }

            object Test {
                const val bomJunit = "androidx.compose.ui:ui-test-junit4"
                const val bomManifest = "androidx.compose.ui:ui-test-manifest"
            }
        }
        const val splashScreen = "androidx.core:core-splashscreen:1.0.1"
    }

    object Utils {
        private const val jodaTimeVersion = "2.9.9.2"
        private const val essentialsVersion = "3.1.0"
        private const val android310Version = "1.4.7"

        const val jodaTime = "net.danlew:android.joda:$jodaTimeVersion"
        const val essentials = "org.greenrobot:essentials:$essentialsVersion"
        const val jsr310 = "com.jakewharton.threetenabp:threetenabp:$android310Version"
    }

    object SocialNetworks {
        private const val fbVersion = "4.29.0"
        private const val vkVersion = "1.6.9"
        private const val okVersion = "2.1.6"

        const val fb = "com.facebook.android:facebook-login:$fbVersion"
        const val vk = "com.vk:androidsdk:$vkVersion"
        const val ok = "ru.ok:odnoklassniki-android-sdk:$okVersion"
    }

    object Yandex {
        private const val liteVersion = "4.0.0-lite"
        private const val fullVersion = "4.0.0-full"
        private const val mapKitVersion = "3.5.0"
        private const val mapKitClusteringVersion = "0.2"
        private const val appMetricaVersion = "6.5.0"

        const val appMetrica = "io.appmetrica.analytics:analytics:$appMetricaVersion"
        const val lite = "com.yandex.android:maps.mobile:$liteVersion"
        const val full = "com.yandex.android:maps.mobile:$fullVersion"
        const val mapKit = "com.yandex.android:mapkit:$mapKitVersion"
        const val mapKitClustering = "com.github.65apps:android-clustering-for-yandex-mapkit:$mapKitClusteringVersion"
    }

    object Google {
        private const val materialVersion = "1.6.1"
        private const val gsonVersion = "2.10.1"

        const val materialDesign = "com.google.android.material:material:$materialVersion"
        const val gson = "com.google.code.gson:gson:$gsonVersion"
        const val guavaConflictLost = "com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava"

        object Services {
            const val servicesVersion = "4.4.0"
            private const val fitnessVersion = "21.1.0"
            private const val authVersion = "20.2.0"
            private const val healthConnectVersion = "1.1.0-alpha10"

            const val fitness = "com.google.android.gms:play-services-fitness:$fitnessVersion"
            const val auth = "com.google.android.gms:play-services-auth:$authVersion"
            const val healthConnect = "androidx.health.connect:connect-client:$healthConnectVersion"

            object MlKit {
                private const val barcodeScanerVersion = "18.1.0"

                const val barcodeScaner = "com.google.android.gms:play-services-mlkit-barcode-scanning:$barcodeScanerVersion"
            }

            object CameraX {
                private const val cameraxVersion = "1.4.1"

                const val core = "androidx.camera:camera-core:$cameraxVersion"
                const val camera2 = "androidx.camera:camera-camera2:$cameraxVersion"
                const val lifecycle = "androidx.camera:camera-lifecycle:$cameraxVersion"
                const val mlKit = "androidx.camera:camera-mlkit-vision:$cameraxVersion"
                const val extentions = "androidx.camera:camera-extensions:$cameraxVersion"
            }
        }

        object FireBase {
            const val pluginVersion = "2.0.0"
            const val crashliticsGradleVersion = "2.9.9"
            private const val bomVersion = "31.4.0"

            const val bom = "com.google.firebase:firebase-bom:$bomVersion"
            const val messagingBom = "com.google.firebase:firebase-messaging-ktx"
            const val storageBom = "com.google.firebase:firebase-storage-ktx"
            const val firestoreBom = "com.google.firebase:firebase-firestore-ktx"
            const val databaseBom = "com.google.firebase:firebase-database-ktx"
            const val analyticsBom = "com.google.firebase:firebase-analytics-ktx"
            const val configBom = "com.google.firebase:firebase-config-ktx"
            const val crashlyticsBom = "com.google.firebase:firebase-crashlytics-ktx"
            const val appMessagingBom = "com.google.firebase:firebase-inappmessaging-display"
            const val dynamicLinksBom = "com.google.firebase:firebase-dynamic-links"
        }

        object GoogleMap {
            private const val mapVersion = "18.0.2"
            private const val mapKtxVersion = "3.3.0"
            private const val locationVersion = "19.0.1"
            private const val utilsVersion = "2.2.3"

            const val maps = "com.google.android.gms:play-services-maps:$mapVersion"
            const val mapKtx = "com.google.maps.android:maps-ktx:$mapKtxVersion"
            const val location = "com.google.android.gms:play-services-location:$locationVersion"
            const val utils = "com.google.maps.android:android-maps-utils:$utilsVersion"
        }
    }

    object Test {
        private const val junitVersion = "4.13.2"
        private const val extJunitVersion = "1.1.5"
        private const val espressoVersion = "3.5.1"
        private const val monitorVersion = "1.6.0"
        private const val testStorageVersion = "1.4.2"
        private const val java310Version = "1.3.1"
        private const val coreTestVersion = "1.5.0"
        private const val rulesVersion = "1.5.0"
        private const val runnerVersion = "1.5.2"
        private const val robolectricVersion = "4.8"
        private const val mockitoKotlinVersion = "2.0.0-RC1"
        private const val mockitoAndroidVersion = "2.8.47"

        const val junit = "junit:junit:$junitVersion"
        const val extJunit = "androidx.test.ext:junit:$extJunitVersion"
        const val extJunitKtx = "androidx.test.ext:junit-ktx:$extJunitVersion"
        const val espresso = "androidx.test.espresso:espresso-core:$espressoVersion"
        const val testStorage = "androidx.test.services:test-services:$testStorageVersion"
        const val monitor = "androidx.test:monitor:$monitorVersion"
        const val rules = "androidx.test:rules:$rulesVersion"
        const val runner = "androidx.test:runner:$runnerVersion"
        const val core = "androidx.test:core:$coreTestVersion"
        const val coreKtx = "androidx.test:core-ktx:$coreTestVersion"
        const val jsr310 = "org.threeten:threetenbp:$java310Version"
        const val kotlinJUnit = "org.jetbrains.kotlin:kotlin-test-junit:${Kotlin.version}"
        const val mockito = "com.nhaarman.mockitokotlin2:mockito-kotlin:$mockitoKotlinVersion"
        const val robolectric = "org.robolectric:robolectric:$robolectricVersion"
        const val mockitoAndroid = "org.mockito:mockito-android:$mockitoAndroidVersion"
    }

    object CustomView {
        private const val pulseViewVersion = "1.0.3"
        private const val pinViewVersion = "1.4.4"
        private const val expandableLayoutVersion = "2.9.2"
        private const val tooltipVersion = "0.1.9"
        private const val inputMaskVersion = "6.1.0"
        private const val materialDialogsVersion = "0.9.4.4"
        private const val datePickerVersion = "2.0.0"
        private const val materialEditTextVersion = "2.1.4"
        private const val cardViewVersion = "1.0.0"
        private const val lottieVersion = "6.0.0"

        const val pulseView = "pl.bclogic:pulsator4droid:$pulseViewVersion"
        const val pinView = "io.github.chaosleung:pinview:$pinViewVersion"
        const val expandableLayout = "com.github.cachapa:ExpandableLayout:$expandableLayoutVersion"
        const val tooltip = "com.github.vihtarb:tooltip:$tooltipVersion"
        const val inputMask = "com.github.RedMadRobot:input-mask-android:$inputMaskVersion"
        const val materialDialogs = "com.afollestad.material-dialogs:core:$materialDialogsVersion"
        const val datePicker = "com.github.prolificinteractive:material-calendarview:$datePickerVersion"
        const val materialEditText = "com.rengwuxian.materialedittext:library:$materialEditTextVersion"
        const val cardView = "com.github.captain-miao:optroundcardview:$cardViewVersion"
        const val lottie = "com.airbnb.android:lottie:$lottieVersion"
    }

    object RxJava2 {
        private const val rxJavaVersion = "2.2.5"
        private const val rxKotlinVersion = "2.4.0"
        private const val rxAndroidVersion = "2.1.0"
        private const val rxRelayVersion = "2.1.0"
        private const val rxLocationVersion = "2.1@aar"
        private const val rxPermissionsVersion = "0.10.0"
        private const val rxPmVersion = "2.1.2"
        private const val rxBindingVersion = "2.0.0"
        private const val rxNetworkVersion = "3.0.2"
        private const val rxReplayingVersion = "2.1.1"

        const val rxJava = "io.reactivex.rxjava2:rxjava:$rxJavaVersion"
        const val rxKotlin = "io.reactivex.rxjava2:rxkotlin:$rxKotlinVersion"
        const val rxAndroid = "io.reactivex.rxjava2:rxandroid:$rxAndroidVersion"
        const val rxPm = "me.dmdev.rxpm:rxpm:$rxPmVersion"
        const val rxBinding = "com.jakewharton.rxbinding2:rxbinding-kotlin:$rxBindingVersion"
        const val rxRelay = "com.jakewharton.rxrelay2:rxrelay:$rxRelayVersion"
        const val rxLocation = "pl.charmas.android:android-reactive-location2:$rxLocationVersion"
        const val rxPermissions = "com.github.tbruyelle:rxpermissions:$rxPermissionsVersion"
        const val rxNetwork = "com.github.pwittchen:reactivenetwork-rx2:$rxNetworkVersion"
        const val rxReplaying = "com.jakewharton.rx2:replaying-share-kotlin:$rxReplayingVersion"
    }

    object Nordic {
        private const val scanerVersion = "1.7.2"
        private const val dfuVersion = "2.5.0"
        private const val bleVersion = "2.10.2"

        const val scanner = "no.nordicsemi.android.support.v18:scanner:$scanerVersion"
        const val dfu = "no.nordicsemi.android:dfu:$dfuVersion"
        const val ble = "no.nordicsemi.android:ble:$bleVersion"
        const val bleKtx = "no.nordicsemi.android:ble-ktx:$bleVersion"
    }

    object ObjectBox {
        const val version = "4.1.0"

        const val core = "io.objectbox:objectbox-kotlin:$version"
        const val browser = "io.objectbox:objectbox-android-objectbrowser:$version"
    }

    object Cicerone {
        private const val version = "7.1"

        const val core = "com.github.terrakok:cicerone:$version"
    }

    object Dagger {
        const val daggerVersion = "2.22.1"
        const val hiltVersion = "2.42"

        private const val javaxAnnotationVersion = "1.3.2"
        private const val javaxInjectVersion = "1"
        private const val glassFishVersion = "10.0-b28"

        const val hiltPlugin = "com.google.dagger.hilt.android"
        const val hilt = "com.google.dagger:hilt-android:$hiltVersion"
        const val hiltCompiler = "com.google.dagger:hilt-android-compiler:$hiltVersion"
        const val dagger = "com.google.dagger:dagger:$daggerVersion"
        const val daggerCompiler = "com.google.dagger:dagger-compiler:$daggerVersion"
        const val daggerAndroid = "com.google.dagger:dagger-android:$daggerVersion"
        const val daggerAndroidProcessor = "com.google.dagger:dagger-android-processor:$daggerVersion"
        const val daggerAndroidSupport = "com.google.dagger:dagger-android-support:$daggerVersion"
        const val javaxAnnotation = "javax.annotation:javax.annotation-api:$javaxAnnotationVersion"
        const val javaxInject = "javax.inject:javax.inject:$javaxInjectVersion"
        const val glassFish = "org.glassfish:javax.annotation:$glassFishVersion"
    }

    object Timber {
        private const val version = "5.0.1"

        const val core = "com.jakewharton.timber:timber:$version"
    }

    object Retrofit {
        private const val version = "2.9.0"

        const val core = "com.squareup.retrofit2:retrofit:$version"
        const val gsonConverter = "com.squareup.retrofit2:converter-gson:$version"
        const val moshiConverter = "com.squareup.retrofit2:converter-moshi:$version"
        const val rxJava2Adapter = "com.squareup.retrofit2:adapter-rxjava2:$version"
    }

    object OkHttp {
        private const val version = "4.12.0"

        const val core = "com.squareup.okhttp3:okhttp:$version"
        const val loggingInterceptor = "com.squareup.okhttp3:logging-interceptor:$version"
    }

    object Coil {
        private const val version = "2.2.2"

        const val core = "io.coil-kt:coil:$version"
        const val compose = "io.coil-kt:coil-compose:$version"
    }

    object DataStore {
        private const val version = "1.0.0"

        const val proto = "androidx.datastore:datastore:$version"
        const val preferences = "androidx.datastore:datastore-preferences:$version"
    }

    object KoDeIn {
        private const val version = "7.12.0"
        private const val versionCompose = "7.12.0"

        const val core = "org.kodein.di:kodein-di:$version"
        const val androidCore = "org.kodein.di:kodein-di-framework-android-core:$version"
        const val androidSupport = "org.kodein.di:kodein-di-framework-android-support:$version"
        const val androidJetpack = "org.kodein.di:kodein-di-framework-android-x:$version"
        const val androidViewModel = "org.kodein.di:kodein-di-framework-android-x-viewmodel:$version"
        const val androidViewModelWithState = "org.kodein.di:kodein-di-framework-android-x-viewmodel-savedstate:$version"
        const val jetpackCompose = "org.kodein.di:kodein-di-framework-compose:$versionCompose"
    }


    object IIOT {
        private const val jacsonVersion = "2.16.0"

        const val JacsonDatabind = "com.fasterxml.jackson.core:jackson-databind:$jacsonVersion"
    }

    object OneSignal {
        private const val version = "5.1.37"

        const val core = "com.onesignal:OneSignal:$version"
    }

}
