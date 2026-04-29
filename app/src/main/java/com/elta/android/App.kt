package com.elta.android

import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.multidex.MultiDex
import com.elta.android.data.di.ApiConstantsModule
import com.elta.android.data.di.InterceptorModule
import com.elta.android.data.features.auth.datasource.social.SocialNetworks
import com.elta.android.domain.features.diary.home.interactor.GetHomeModelUseCase
import com.elta.android.presentation.di.AnalyticModule
import com.elta.android.presentation.features.app.ui.AppActivity
import com.elta.android.presentation.features.glucose.widget.di.GlucoseWidgetDependencies
import com.elta.android.presentation.features.profile.settings.reminders.utils.RemindersManager
import com.elta.android.presentation.utils.LocaleHelper
import com.google.firebase.FirebaseApp
import com.jakewharton.threetenabp.AndroidThreeTen
import com.nullgr.core.hardware.NetworkChecker
import com.nullgr.core.preferences.defaultPrefs
import com.onesignal.OneSignal
import com.onesignal.notifications.INotificationClickEvent
import com.onesignal.notifications.INotificationClickListener
import com.onesignal.notifications.INotificationLifecycleListener
import com.onesignal.notifications.INotificationWillDisplayEvent
import com.yandex.mapkit.MapKitFactory
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.HasBroadcastReceiverInjector
import dagger.android.HasServiceInjector
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig
import io.reactivex.plugins.RxJavaPlugins
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber
import javax.inject.Inject

class App : Application(), HasActivityInjector, HasBroadcastReceiverInjector, HasServiceInjector,
    GlucoseWidgetDependencies {

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

    @Inject lateinit var networkChecker: NetworkChecker
    @Inject
    override lateinit var getHomeModelUseCase: GetHomeModelUseCase

    override fun onCreate() {
        super.onCreate()
        LocaleHelper.onAttach(this)
        initFirebase()
        initInjector()
        initLogger()
        initTime()
        initSocialNetworks()
        initYandexMapKit()
        initRxJava()
        initAppMetric()
        //initScreenConfigs()
        initOneSignal()
    }

    private fun initOneSignal() {
        OneSignalInitializer.initialize(this)
    }

    private fun initRxJava() {
        RxJavaPlugins.setErrorHandler { Timber.e(it, "RxJava global error: ") }
    }

    private fun initFirebase() {
        FirebaseApp.initializeApp(this)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(base))
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
            .analyticsModule(AnalyticModule(this))
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

    private fun initAppMetric() {
        val key =
            if (BuildConfig.DEBUG) com.elta.android.presentation.R.string.app_metric_debug_api_key
            else com.elta.android.presentation.R.string.app_metric_prod_api_key

        val config = AppMetricaConfig
            .newConfigBuilder(resources.getString(key))
            .build()
        AppMetrica.activate(this, config)
    }
}
