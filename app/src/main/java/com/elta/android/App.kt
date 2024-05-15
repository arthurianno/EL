package com.elta.android

import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import androidx.multidex.MultiDex
import com.elta.android.data.di.ApiConstantsModule
import com.elta.android.data.di.InterceptorModule
import com.elta.android.data.features.auth.datasource.social.SocialNetworks
import com.elta.android.presentation.di.AnalyticsModule
import com.elta.android.presentation.features.profile.settings.reminders.utils.RemindersManager
import com.google.firebase.FirebaseApp
import com.jakewharton.threetenabp.AndroidThreeTen
import com.nullgr.core.preferences.defaultPrefs
import com.yandex.mapkit.MapKitFactory
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.HasBroadcastReceiverInjector
import dagger.android.HasServiceInjector
import io.reactivex.plugins.RxJavaPlugins
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber
import javax.inject.Inject

class App : Application(), HasActivityInjector, HasBroadcastReceiverInjector, HasServiceInjector {

    @Inject
    lateinit var dispatchingActivityInjector: DispatchingAndroidInjector<Activity>

    @Inject
    lateinit var dispatchingReceiverInjector: DispatchingAndroidInjector<BroadcastReceiver>

    @Inject
    lateinit var dispatchingServiceInjector: DispatchingAndroidInjector<Service>

    @Inject
    lateinit var logTree: Timber.Tree

    @Inject
    lateinit var remindersManager: RemindersManager

    override fun onCreate() {
        super.onCreate()
        initFirebase()
        initInjector()
        initLogger()
        initTime()
        initSocialNetworks()
        initYandexMapKit()
        initRxJava()
    }

    private fun initRxJava() {
        RxJavaPlugins.setErrorHandler { Timber.e(it, "RxJava global error: ") }
    }

    private fun initFirebase() {
        FirebaseApp.initializeApp(this)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun activityInjector(): AndroidInjector<Activity> = dispatchingActivityInjector

    override fun broadcastReceiverInjector(): AndroidInjector<BroadcastReceiver> =
        dispatchingReceiverInjector

    override fun serviceInjector(): AndroidInjector<Service> =
        dispatchingServiceInjector

    private fun initInjector() {
        DaggerAppComponent
            .builder()
            .context(this)
            .appModule(AppModule(this, BuildConfig.IS_LOG_ENABLED, BuildConfig.DEBUG))
            .apiConstantsModule(
                ApiConstantsModule(
                    defaultPrefs(this),
                    BuildConfig.SERVER_URL,
                    BuildConfig.DEBUG
                )
            )
            .interceptorModule(
                InterceptorModule(
                    App::class.java.simpleName,
                    if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.BASIC
                )
            )
            .analyticsModule(AnalyticsModule(this))
            .build()
            .inject(this)
    }

    private fun initLogger() {
        Timber.plant(logTree)
    }

    private fun initTime() {
        AndroidThreeTen.init(this)
    }

    private fun initYandexMapKit() {
        MapKitFactory.setApiKey(resources.getString(com.elta.android.presentation.R.string.yandex_map_api_key))
    }

    private fun initSocialNetworks() {
        SocialNetworks.initialize(this)
    }
}
