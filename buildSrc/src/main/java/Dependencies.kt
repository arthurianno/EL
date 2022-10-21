object Dependencies {
    const val gradleVersion = "7.3.1"
    const val secretGradlePluginVersion = "2.0.1"
    const val ktLintVersion = "10.3.0"
    const val dependenciesUpdateVersion = "0.42.0"

    object Jetpack {
        private object Version {
            const val core = "1.8.0"
            const val fragment = "1.4.1"
            const val lifeCycleVersion = "2.5.0"
            const val paging = "3.1.1"
            const val constraintLayout = "2.1.4"
            const val appcompat = "1.4.2"
        }

        const val core = "androidx.core:core-ktx:${Version.core}"
        const val fragment = "androidx.fragment:fragment-ktx:${Version.fragment}"
        const val viewModel =
            "androidx.lifecycle:lifecycle-viewmodel-ktx:${Version.lifeCycleVersion}"
        const val lifeCycle = "androidx.lifecycle:lifecycle-runtime-ktx:${Version.lifeCycleVersion}"
        const val paging = "androidx.paging:paging-runtime:${Version.paging}"
        const val appCompat = "androidx.appcompat:appcompat:${Version.appcompat}"
        const val constraintLayout =
            "androidx.constraintlayout:constraintlayout:${Version.constraintLayout}"

        object Compose {
            const val version = "1.2.1"
            const val compilerVersion = "1.3.2"

            private object Version {
                const val activity = "1.5.1"
                const val constraintLayout = "1.0.1"
                const val paging = "1.0.0-alpha15"
                const val materialThemeAdapter = "1.1.3"
            }

            const val ui = "androidx.compose.ui:ui:$version"
            const val runtime = "androidx.compose.runtime:runtime:$version"
            const val activity = "androidx.activity:activity-compose:${Version.activity}"
            const val material = "androidx.compose.material:material:$version"
            const val animation = "androidx.compose.animation:animation:$version"
            const val uiTooling = "androidx.compose.ui:ui-tooling:$version"
            const val uiToolingPreview = "androidx.compose.ui:ui-tooling-preview:$version"
            const val constraintLayout =
                "androidx.constraintlayout:constraintlayout-compose:${Version.constraintLayout}"
            const val viewModel =
                "androidx.lifecycle:lifecycle-viewmodel-compose:${Jetpack.Version.lifeCycleVersion}"
            const val paging = "androidx.paging:paging-compose:${Version.paging}"
            const val MaterialThemeAdapter =
                "com.google.android.material:compose-theme-adapter:${Version.materialThemeAdapter}"

            object Accompanist {
                private object Version {
                    const val stable = "0.23.1"
                    const val latest = "0.24.10-beta"
                }

                private const val version = Version.latest

                const val drawablePainter =
                    "com.google.accompanist:accompanist-drawablepainter:$version"
                const val flowlayout = "com.google.accompanist:accompanist-flowlayout:$version"
                const val insets = "com.google.accompanist:accompanist-insets:$version"
                const val insetsUi = "com.google.accompanist:accompanist-insets-ui:$version"
                const val navigationAnimation =
                    "com.google.accompanist:accompanist-navigation-animation:$version"
                const val navigationMaterial =
                    "com.google.accompanist:accompanist-navigation-material:$version"
                const val pager = "com.google.accompanist:accompanist-pager:$version"
                const val pagerIndicators =
                    "com.google.accompanist:accompanist-pager-indicators:$version"
                const val placeholder = "com.google.accompanist:accompanist-placeholder:$version"
                const val placeholderMaterial =
                    "com.google.accompanist:accompanist-placeholder-material:$version"
                const val systemUiController =
                    "com.google.accompanist:accompanist-systemuicontroller:$version"
                const val webView = "com.google.accompanist:accompanist-webview:$version"
                const val permissions = "com.google.accompanist:accompanist-permissions:$version"
                const val swipeRefresh = "com.google.accompanist:accompanist-swiperefresh:$version"
                const val theme = "com.google.accompanist:accompanist-appcompat-theme:$version"
            }

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
        const val version = "1.7.10"

        private object Version {
            const val serialization = "1.4.0"
            const val coroutines = "1.6.4"
            const val dateTime = "0.3.1"
        }

        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib:$version"
        const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Version.coroutines}"
        const val serialization =
            "org.jetbrains.kotlinx:kotlinx-serialization-core:${Version.serialization}"
        const val serializationJson =
            "org.jetbrains.kotlinx:kotlinx-serialization-json:${Version.serialization}"
        const val dateTime = "org.jetbrains.kotlinx:kotlinx-datetime:${Version.dateTime}"
    }

    object Yandex {
        private object Version {
            const val lite = "4.0.0-lite"
            const val full = "4.0.0-full"
        }

        const val lite = "com.yandex.android:maps.mobile:${Version.lite}"
        const val full = "com.yandex.android:maps.mobile:${Version.full}"
    }

    object Google {
        private object Version {
            const val material = "1.6.1"
        }

        const val material = "com.google.android.material:material:${Version.material}"

        object Services {
            private object Version {
                const val fitness = "21.1.0"
                const val auth = "20.2.0"
            }

            const val fitness = "com.google.android.gms:play-services-fitness:${Version.fitness}"
            const val auth = "com.google.android.gms:play-services-auth:${Version.auth}"
        }

        object FireBase {
            private const val bomVersion = "30.1.0"

            const val bom = "com.google.firebase:firebase-bom:$bomVersion}"
            const val messaging = "com.google.firebase:firebase-messaging-ktx"
            const val storage = "com.google.firebase:firebase-storage-ktx"
            const val firestore = "com.google.firebase:firebase-firestore-ktx"
            const val database = "com.google.firebase:firebase-database-ktx"
            const val analytics = "com.google.firebase:firebase-analytics-ktx"
            const val crashlytics = "com.google.firebase:firebase-crashlytics-ktx"
        }

        object GoogleMap {
            private object Version {
                const val map = "18.0.2"
                const val mapKtx = "3.3.0"
                const val location = "19.0.1"
                const val utils = "2.2.3"
            }

            const val map = "com.google.android.gms:play-services-maps:${Version.map}"
            const val mapKtx = "com.google.maps.android:maps-ktx:${Version.mapKtx}"
            const val location = "com.google.android.gms:play-services-location:${Version.location}"
            const val utils = "com.google.maps.android:android-maps-utils:${Version.utils}"
        }
    }

    object Test {
        private object Version {
            const val junit = "4.13.2"
            const val junitExt = "1.1.3"
            const val espresso = "3.4.0"
        }

        const val junit = "junit:junit:${Version.junit}"
        const val junitExt = "androidx.test.ext:junit:${Version.junitExt}"
        const val espresso = "androidx.test.espresso:espresso-core:${Version.espresso}"
        const val composeUi = "androidx.compose.ui:ui-test-junit4:${Jetpack.Compose.version}"
        const val composeUiTestManifest =
            "androidx.compose.ui:ui-test-manifest:${Jetpack.Compose.version}"
    }

    object ObjectBox {
        const val version = "3.1.3"
    }

    object Dagger {
        const val version = "2.42"

        const val hilt = "com.google.dagger:hilt-android:$version"
        const val hiltCompiler = "com.google.dagger:hilt-android-compiler:$version"
    }

    object Retrofit {
        private const val version = "2.9.0"
        const val core = "com.squareup.retrofit2:retrofit:$version"
        const val json = "com.squareup.retrofit2:converter-gson:$version"
        const val moshi = "com.squareup.retrofit2:converter-moshi:$version"
    }

    object OkHttp {
        private const val version = "5.0.0-alpha.8"
        const val core = "com.squareup.okhttp3:okhttp:$version"
        const val okhttpLoggingInterceptor = "com.squareup.okhttp3:logging-interceptor:$version"
    }

    object Glide {
        private const val version = "4.12.0"
        const val core = "com.github.bumptech.glide:glide:$version"
        const val compiler = "com.github.bumptech.glide:compiler:$version"
    }

    object Coil {
        private const val version = "2.1.0"

        const val core = "io.coil-kt:coil:$version"
        const val compose = "io.coil-kt:coil-compose:$version"
    }

    object Room {
        private const val version = "2.4.2"

        const val core = "androidx.room:room-runtime:$version"
        const val compiler = "androidx.room:room-compiler:$version"
    }

    object DataStore {
        private const val version = "1.0.0"

        const val proto = "androidx.datastore:datastore:$version"
        const val preferences = "androidx.datastore:datastore-preferences:$version"
    }

    object Ktor {
        private const val version = "2.0.2"

        const val core = "io.ktor:ktor-client-core:$version"
        const val cio = "io.ktor:ktor-client-cio:$version"
        const val logging = "io.ktor:ktor-client-logging:$version"
        const val contentNegotiation = "io.ktor:ktor-client-content-negotiation:$version"
        const val serializationJson = "io.ktor:ktor-serialization-kotlinx-json:$version"
        const val android = "io.ktor:ktor-client-android:$version"
        const val okhttp = "io.ktor:ktor-client-okhttp:$version"
        const val ios = "io.ktor:ktor-client-ios:$version"
    }

    object SqlDelight {
        const val version = "1.5.3"

        const val core = "com.squareup.sqldelight:runtime:$version"
        const val coroutines = "com.squareup.sqldelight:coroutines-extensions:$version"
        const val androidDriver = "com.squareup.sqldelight:android-driver:$version"
        const val nativeDriver = "com.squareup.sqldelight:native-driver:$version"
        const val paging3 = "com.squareup.sqldelight:android-paging3-extensions:$version"
    }

    object Decompose {
        private const val version = "1.0.0-alpha-02"

        const val core = "com.arkivanov.decompose:decompose:$version"
        const val jetpack = "com.arkivanov.decompose:extensions-compose-jetpack:$version"
        const val jetbrains = "com.arkivanov.decompose:extensions-compose-jetbrains:$version"
        const val android = "com.arkivanov.decompose:extensions-android:$version"
    }

    object KoDeIn {
        private const val version = "7.14.0"
        private const val versionCompose = "7.14.0"

        const val core = "org.kodein.di:kodein-di:$version"
        const val androidCore = "org.kodein.di:kodein-di-framework-android-core:$version"
        const val androidSupport = "org.kodein.di:kodein-di-framework-android-support:$version"
        const val androidJetpack = "org.kodein.di:kodein-di-framework-android-x:$version"
        const val androidViewModel =
            "org.kodein.di:kodein-di-framework-android-x-viewmodel:$version"
        const val androidViewModelWithState =
            "org.kodein.di:kodein-di-framework-android-x-viewmodel-savedstate:$version"
        const val jetpackCompose = "org.kodein.di:kodein-di-framework-compose:$versionCompose"
    }
}
