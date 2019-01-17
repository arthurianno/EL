package com.elta.android

import android.app.Activity
import android.app.Application
import android.content.Context
import android.support.multidex.MultiDex
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.elta.android.data.di.ApiConstantsModule
import com.elta.android.data.di.InterceptorModule
import com.elta.android.presentation.di.AnalyticsModule
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import io.fabric.sdk.android.Fabric
import net.danlew.android.joda.JodaTimeAndroid
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber
import javax.inject.Inject

class App : Application(), HasActivityInjector {

    @Inject
    lateinit var dispatchingActivityInjector: DispatchingAndroidInjector<Activity>

    @Inject
    lateinit var logTree: Timber.Tree

    override fun onCreate() {
        super.onCreate()
        initializeCrashlytics()
        initializeInjector()
        initializeLogger()
        initializeJodaTime()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun activityInjector(): AndroidInjector<Activity> = dispatchingActivityInjector

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

    private fun initializeJodaTime() {
        JodaTimeAndroid.init(this)
    }

    private fun initializeCrashlytics() {
        val crashlyticsKit = Crashlytics.Builder()
            .core(CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build()).build()
        Fabric.with(this, crashlyticsKit)
    }
}