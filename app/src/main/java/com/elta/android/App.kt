package com.elta.android

import android.app.Activity
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.support.multidex.MultiDex
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.elta.android.data.di.ApiConstantsModule
import com.elta.android.data.di.InterceptorModule
import com.elta.android.data.features.auth.datasource.social.SocialNetworks
import com.elta.android.presentation.core.date.DateChangeReceiver
import com.elta.android.presentation.di.AnalyticsModule
import com.elta.android.presentation.features.profile.settings.reminders.utils.RemindersManager
import com.jakewharton.threetenabp.AndroidThreeTen
import com.nullgr.core.rx.RxBus
import com.yandex.mapkit.MapKitFactory
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.HasBroadcastReceiverInjector
import io.fabric.sdk.android.Fabric
import io.reactivex.plugins.RxJavaPlugins
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber
import javax.inject.Inject

class App : Application(), HasActivityInjector, HasBroadcastReceiverInjector {

    @Inject
    lateinit var dispatchingActivityInjector: DispatchingAndroidInjector<Activity>

    @Inject
    lateinit var dispatchingReceiverInjector: DispatchingAndroidInjector<BroadcastReceiver>

    @Inject
    lateinit var logTree: Timber.Tree

    @Inject
    lateinit var remindersManager: RemindersManager

    @Inject
    lateinit var bus: RxBus

    lateinit var dateReceiver: DateChangeReceiver

    override fun onCreate() {
        super.onCreate()
        initializeCrashlytics()
        initializeInjector()
        initializeLogger()
        initializeTime()
        initializeSocialNetworks()
        initalizeYandexMapKit()
        initializeDateReceiver()
        RxJavaPlugins.setErrorHandler { Timber.e(it, "RxJava global error: ") }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun activityInjector(): AndroidInjector<Activity> = dispatchingActivityInjector

    override fun broadcastReceiverInjector(): AndroidInjector<BroadcastReceiver> = dispatchingReceiverInjector

    private fun initializeInjector() {
        DaggerAppComponent
            .builder()
            .context(this)
            .appModule(AppModule(BuildConfig.IS_LOG_ENABLED))
            .apiConstantsModule(ApiConstantsModule(BuildConfig.DEBUG))
            .interceptorModule(InterceptorModule(App::class.java.simpleName, HttpLoggingInterceptor.Level.BODY))
            .analyticsModule(AnalyticsModule(this))
            .build()
            .inject(this)
    }

    private fun initializeLogger() {
        Timber.plant(logTree)
    }

    private fun initializeTime() {
        AndroidThreeTen.init(this)
    }

    private fun initalizeYandexMapKit() {
        MapKitFactory.setApiKey(resources.getString(R.string.yandex_map_api_key))
    }

    private fun initializeCrashlytics() {
        val crashlyticsKit = Crashlytics.Builder()
            .core(CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build()).build()
        Fabric.with(this, crashlyticsKit)
    }

    private fun initializeSocialNetworks() {
        SocialNetworks.initialize(this)
    }

    private fun initializeDateReceiver() {
        dateReceiver = DateChangeReceiver(bus)
        dateReceiver.register(this)
    }
}