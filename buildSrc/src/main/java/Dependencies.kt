object Dependencies {
    const val gradleVersion = "7.3.0"
    const val kotlinVersion = "1.7.10"
    const val ktLintVersion = "10.3.0"
    const val dependenciesUpdateVersion = "0.42.0"
    const val detektGradlePluginVersion = "1.22.0-RC2"

    object Jetpack {
        private const val coreVersion = "1.8.0"
        private const val fragmentVersion = "1.4.1"
        private const val lifeCycleVersion = "2.5.0"
        private const val pagingVersion = "3.1.1"
        private const val constraintLayoutVersion = "2.1.4"
        private const val appcompatVersion = "1.4.2"
        private const val mutlidexVersion = "2.0.1"
        private const val recyclerViewVersion = "1.2.1"
        private const val annotationsVersion = "1.5.0"
        private const val browserVersion = "1.4.0"

        const val core = "androidx.core:core-ktx:$coreVersion"
        const val fragment = "androidx.fragment:fragment-ktx:$fragmentVersion"
        const val viewModel = "androidx.lifecycle:lifecycle-viewmodel-ktx:$lifeCycleVersion"
        const val lifeCycle = "androidx.lifecycle:lifecycle-runtime-ktx:$lifeCycleVersion"
        const val paging = "androidx.paging:paging-runtime:$pagingVersion"
        const val appCompat = "androidx.appcompat:appcompat:$appcompatVersion"
        const val constraintLayout =
            "androidx.constraintlayout:constraintlayout:$constraintLayoutVersion"
        const val multiDex = "androidx.multidex:multidex:$mutlidexVersion"
        const val reciclerView = "androidx.recyclerview:recyclerview:$recyclerViewVersion"
        const val annotations = "androidx.annotation:annotation:$annotationsVersion"
        const val browser = "androidx.browser:browser:$browserVersion"

        object Compose {
            const val composeVersion = "1.2.1" // 1.2.1
            const val compilerVersion = "1.3.0" // 1.3.2

            private const val activityVersion = "1.5.1"
            private const val constraintLayoutVersion = "1.0.1"
            private const val pagingVersion = "1.0.0-alpha15"
            private const val materialThemeAdapterVersion = "1.1.3"

            const val ui = "androidx.compose.ui:ui:$composeVersion"
            const val runtime = "androidx.compose.runtime:runtime:$composeVersion"
            const val activity = "androidx.activity:activity-compose:$activityVersion"
            const val material = "androidx.compose.material:material:$composeVersion"
            const val foundation = "androidx.compose.foundation:foundation:$composeVersion"
            const val animation = "androidx.compose.animation:animation:$composeVersion"
            const val uiTooling = "androidx.compose.ui:ui-tooling:$composeVersion"
            const val uiToolingPreview = "androidx.compose.ui:ui-tooling-preview:$composeVersion"
            const val constraintLayout =
                "androidx.constraintlayout:constraintlayout-compose:$constraintLayoutVersion"
            const val viewModel = "androidx.lifecycle:lifecycle-viewmodel-compose:$lifeCycleVersion"
            const val paging = "androidx.paging:paging-compose:$pagingVersion"
            const val MaterialThemeAdapter =
                "com.google.android.material:compose-theme-adapter:$materialThemeAdapterVersion"
            const val rxJava2 = "androidx.compose.runtime:runtime-rxjava2:$composeVersion"

            object Voyager {
                private const val version = "1.0.0-rc02"

                const val core = "cafe.adriel.voyager:voyager-navigator:$version"
                const val bottomSheet =
                    "cafe.adriel.voyager:voyager-bottom-sheet-navigator:$version"
                const val tab = "cafe.adriel.voyager:voyager-tab-navigator:$version"
                const val transitions =
                    "cafe.adriel.voyager:voyager-transitions:$version"
                const val viewModel = "cafe.adriel.voyager:voyager-androidx:$version"
                const val koin = "cafe.adriel.voyager:voyager-koin:$version"
                const val kodein = "cafe.adriel.voyager:voyager-kodein:$version"
                const val hilt = "cafe.adriel.voyager:voyager-hilt:$version"
                const val rxJava = "cafe.adriel.voyager:voyager-rxjava:$version"
                const val liveData = "cafe.adriel.voyager:voyager-livedata:$version"
            }
        }

        object Navigation {
            private const val version = "2.4.2"

            const val fragment = "androidx.navigation:navigation-fragment-ktx:$version"
            const val ui = "androidx.navigation:navigation-ui-ktx:$version"
            const val runtime = "androidx.navigation:navigation-runtime-ktx:$version"
        }
    }

    object Kotlin {
        private const val serializationVersion = "1.4.0"
        private const val coroutinesVersion = "1.6.4"
        private const val dateTimeVersion = "0.3.1"

        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion"
        const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion"
        const val serialization =
            "org.jetbrains.kotlinx:kotlinx-serialization-core:$serializationVersion"
        const val serializationJson =
            "org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion"
        const val dateTime = "org.jetbrains.kotlinx:kotlinx-datetime:$dateTimeVersion"
    }

    object Utils {
        private const val jodaTimeVersion = "2.9.9.2" // 2.12.0
        private const val essentialsVersion = "3.1.0"
        private const val android310Version = "1.2.0"

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
        private const val mapKitVersion = "3.3.1"
        private const val mapKitClusteringVersion = "0.2"

        const val lite = "com.yandex.android:maps.mobile:$liteVersion"
        const val full = "com.yandex.android:maps.mobile:$fullVersion"
        const val mapKit = "com.yandex.android:mapkit:$mapKitVersion"
        const val mapKitClustering =
            "com.github.65apps:android-clustering-for-yandex-mapkit:$mapKitClusteringVersion"
    }

    object Google {
        private const val materialVersion = "1.6.1"
        private const val gsonVersion = "2.8.5" // 2.9.1

        const val materialDesign = "com.google.android.material:material:$materialVersion"
        const val gson = "com.google.code.gson:gson:$gsonVersion"
        const val guavaConflictLost =
            "com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava"

        object Services {
            const val servicesVersion = "4.3.14"
            private const val fitnessVersion = "21.1.0"
            private const val authVersion = "20.2.0"

            const val fitness = "com.google.android.gms:play-services-fitness:$fitnessVersion"
            const val auth = "com.google.android.gms:play-services-auth:$authVersion"
        }

        object FireBase {
            const val pluginVersion = "2.0.0"
            const val crashliticsGradleVersion = "2.9.0"
            private const val bomVersion = "31.0.1"

            const val bom = "com.google.firebase:firebase-bom:$bomVersion"
            const val messagingBom = "com.google.firebase:firebase-messaging-ktx"
            const val storageBom = "com.google.firebase:firebase-storage-ktx"
            const val firestoreBom = "com.google.firebase:firebase-firestore-ktx"
            const val databaseBom = "com.google.firebase:firebase-database-ktx"
            const val analyticsBom = "com.google.firebase:firebase-analytics-ktx"
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
        private const val junitExtVersion = "1.1.3"
        private const val espressoVersion = "3.4.0"
        private const val java310Version = "1.3.1" // 1.6.3
        private const val androidxVersion = "1.4.0"
        private const val robolectricVersion = "4.8"
        private const val mockitoKotlinVersion = "2.0.0-RC1"
        private const val mockitoAndroidVersion = "2.8.47"

        const val junit = "junit:junit:$junitVersion"
        const val junitExt = "androidx.test.ext:junit-ktx:$junitExtVersion"
        const val espresso = "androidx.test.espresso:espresso-core:$espressoVersion"
        const val composeUi = "androidx.compose.ui:ui-test-junit4:${Jetpack.Compose.composeVersion}"
        const val composeUiTestManifest =
            "androidx.compose.ui:ui-test-manifest:${Jetpack.Compose.composeVersion}"

        const val rules = "androidx.test:rules:$androidxVersion"
        const val runner = "androidx.test:runner:$androidxVersion"
        const val core = "androidx.test:core:$androidxVersion"
        const val coreKtx = "androidx.test:core-ktx$androidxVersion"

        const val jsr310 = "org.threeten:threetenbp:$java310Version"

        const val kotlinJUnit = "org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion"
        const val mockito =
            "com.nhaarman.mockitokotlin2:mockito-kotlin:$mockitoKotlinVersion"
        const val robolectric = "org.robolectric:robolectric:$robolectricVersion"
        const val mockitoAndroid = "org.mockito:mockito-android:$mockitoAndroidVersion"
    }

    object CustomView {
        private const val pulseViewVersion = "1.0.3"
        private const val pinViewVersion = "1.4.4"
        private const val expandableLayoutVersion = "2.9.2"
        private const val tooltipVersion = "0.1.9"
        private const val inputMaskVersion = "6.1.0"
        private const val materialDialogsVersion = "0.9.4.4" // Верхняя версия 3.3.0
        private const val datePickerVersion = "2.0.0"
        private const val materialEditTextVersion = "2.1.4"
        private const val cardViewVersion = "1.0.0"

        const val pulseView = "pl.bclogic:pulsator4droid:$pulseViewVersion"
        const val pinView = "io.github.chaosleung:pinview:$pinViewVersion"
        const val expandableLayout = "com.github.cachapa:ExpandableLayout:$expandableLayoutVersion"
        const val tooltip = "com.github.vihtarb:tooltip:$tooltipVersion"
        const val inputMask = "com.github.RedMadRobot:input-mask-android:$inputMaskVersion"
        const val materialDialogs = "com.afollestad.material-dialogs:core:$materialDialogsVersion"
        const val datePicker =
            "com.github.prolificinteractive:material-calendarview:$datePickerVersion"
        const val materialEditText =
            "com.rengwuxian.materialedittext:library:$materialEditTextVersion"
        const val cardView = "com.github.captain-miao:optroundcardview:$cardViewVersion"
    }

    object RxJava2 {
        private const val rxJavaVersion = "2.2.5"
        private const val rxKotlinVersion = "2.4.0"
        private const val rxAndroidVersion = "2.1.0"
        private const val rxRelayVersion = "2.1.0"
        private const val rxLocationVersion = "2.1@aar"
        private const val rxPermissionsVersion = "0.10.2"
        private const val rxPmVersion = "2.1.2"
        private const val rxBindingVersion = "2.0.0"
        private const val rxNetworkVersion = "3.0.2"
        private const val rxReplayingVersion = "2.1.1"
        private const val rxBluetoothVersion = "1.8.2"

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
        const val rxBluetooth = "com.polidea.rxandroidble2:rxandroidble:$rxBluetoothVersion"
    }

    object Nordic {
        private const val scanerVersion = "1.3.1"
        private const val dfuVersion = "2.0.3"

        const val scanner = "no.nordicsemi.android.support.v18:scanner:$scanerVersion"
        const val dfu = "no.nordicsemi.android:dfu:$dfuVersion"
    }

    object ObjectBox {
        const val version = "3.1.3"

        const val core = "io.objectbox:objectbox-kotlin:$version"
        const val browser = "io.objectbox:objectbox-android-objectbrowser:$version"
    }

    object Cicerone {
        private const val version = "7.1"

        const val core = "com.github.terrakok:cicerone:$version"
    }

    object Dagger {
        const val version = "2.22.1" // 2.42

        private const val javaxAnnotationVersion = "1.3.2"
        private const val javaxInjectVersion = "1"
        private const val glassFishVersion = "10.0-b28"

        const val hilt = "com.google.dagger:hilt-android:$version"
        const val hiltCompiler = "com.google.dagger:hilt-android-compiler:$version"
        const val dagger = "com.google.dagger:dagger:$version"
        const val daggerCompiler = "com.google.dagger:dagger-compiler:$version"
        const val daggerAndroid = "com.google.dagger:dagger-android:$version"
        const val daggerAndroidProcessor =
            "com.google.dagger:dagger-android-processor:$version"
        const val daggerAndroidSupport = "com.google.dagger:dagger-android-support:$version"
        const val javaxAnnotation =
            "javax.annotation:javax.annotation-api:$javaxAnnotationVersion"
        const val javaxInject = "javax.inject:javax.inject:$javaxInjectVersion"
        const val glassFish = "org.glassfish:javax.annotation:$glassFishVersion"
    }

    object Timber {
        private const val version = "4.7.1"

        const val core = "com.jakewharton.timber:timber:$version"
    }

    object Retrofit {
        private const val version = "2.5.0" // 2.9.0

        const val core = "com.squareup.retrofit2:retrofit:$version"
        const val gsonConverter = "com.squareup.retrofit2:converter-gson:$version"
        const val moshiConverter = "com.squareup.retrofit2:converter-moshi:$version"
        const val rxJava2Adapter = "com.squareup.retrofit2:adapter-rxjava2:$version"
    }

    object OkHttp {
        private const val version = "3.12.1" // 4.10.0

        const val core = "com.squareup.okhttp3:okhttp:$version"
        const val loggingInterceptor = "com.squareup.okhttp3:logging-interceptor:$version"
    }

    object Coil {
        private const val version = "2.1.0"

        const val core = "io.coil-kt:coil:$version"
        const val compose = "io.coil-kt:coil-compose:$version"
    }

    object DataStore {
        private const val version = "1.0.0"

        const val proto = "androidx.datastore:datastore:$version"
        const val preferences = "androidx.datastore:datastore-preferences:$version"
    }
}
