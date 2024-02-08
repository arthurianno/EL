package com.elta.android

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.elta.android.common.di.qualifires.ComputationFacade
import com.elta.android.common.logger.DebugTree
import com.elta.android.common.logger.FirebaseStorage
import com.elta.android.common.logger.ReleaseTree
import com.elta.android.common.logger.crashlyrics.CrashlyticsReport
import com.elta.android.common.logger.crashlyrics.FirebaseReport
import com.elta.android.presentation.core.geo.RxLocationManagerFixed
import com.elta.android.presentation.core.pm.ExceptionParser
import com.elta.android.presentation.core.pm.SimpleExceptionParser
import com.nullgr.core.adapter.DiffCalculator
import com.nullgr.core.adapter.RxDiffCalculator
import com.nullgr.core.hardware.NetworkChecker
import com.nullgr.core.preferences.defaultPrefs
import com.nullgr.core.resources.ResourceProvider
import com.nullgr.core.rx.RxBus
import com.nullgr.core.rx.SingletonRxBusProvider
import com.nullgr.core.rx.schedulers.ComputationSchedulersFacade
import com.nullgr.core.rx.schedulers.IoToMainSchedulersFacade
import com.nullgr.core.rx.schedulers.SchedulersFacade
import com.nullgr.core.security.prefs.CryptoPreferences
import dagger.Module
import dagger.Provides
import timber.log.Timber
import javax.inject.Singleton

@Suppress("TooManyFunctions")
@Module
class AppModule(
    private val app: Application,
    private val enableLog: Boolean,
    private val isDebug: Boolean
) {

    @Singleton
    @Provides
    fun provideApplication(): Application = app

    @Singleton
    @Provides
    fun provideSchedulersFacade(): SchedulersFacade = IoToMainSchedulersFacade()

    @Singleton
    @Provides
    @ComputationFacade
    fun provideComputationSchedulersFacade(): SchedulersFacade = ComputationSchedulersFacade()

    @Singleton
    @Provides
    fun provideResourceProvider(context: Context): ResourceProvider = ResourceProvider(context)

    @Singleton
    @Provides
    fun provideSharedPreferences(context: Context): SharedPreferences = defaultPrefs(context)

    @Singleton
    @Provides
    fun provideNetworkChecker(context: Context): NetworkChecker = NetworkChecker(context)

    @Singleton
    @Provides
    fun provideRxBus(): RxBus = SingletonRxBusProvider.BUS

    @Provides
    fun provideDiffCalculator(): DiffCalculator = RxDiffCalculator()

    @Provides
    @Singleton
    fun provideErrorParser(): ExceptionParser =
        SimpleExceptionParser()

    @Provides
    @Singleton
    fun provideLogTree(firebaseStorage: FirebaseStorage): Timber.Tree {
        val logsFile = firebaseStorage.localLogsFile
        return if (isDebug) {
            DebugTree(logsFile, enableLog)
        } else {
            ReleaseTree(logsFile, enableLog)
        }
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(context: Context): FirebaseStorage = run {
        FirebaseStorage(context)
    }

    @Provides
    @Singleton
    fun provideCrashlyticsReport(): CrashlyticsReport = FirebaseReport()

    @Provides
    @Singleton
    fun provideCryptoPreferences(context: Context): CryptoPreferences =
        CryptoPreferences(context, context.getString(R.string.crypto_key_alias))

    @Provides
    @Singleton
    fun provideRxLocationManager(context: Context): RxLocationManagerFixed =
        RxLocationManagerFixed(context, updatesInterval = 1100, updateCount = 1)
}
