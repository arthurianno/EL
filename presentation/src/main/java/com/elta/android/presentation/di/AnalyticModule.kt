package com.elta.android.presentation.di

import android.content.Context
import com.elta.android.presentation.analytic.core.analytics.Analytics
import com.elta.android.presentation.analytic.core.analytics.AnalyticsTracker
import com.elta.android.presentation.analytic.core.analytics.FirebaseTracker
import com.elta.android.presentation.analytic.core.appmetric.AppMetricTracker
import com.elta.android.presentation.analytic.core.appmetric.AppMetricTrackerImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

@Module(includes = [AnalyticModule.Declarations::class])
class AnalyticModule(val context: Context) {

    @Module
    interface Declarations {
        @Binds
        @IntoMap
        @StringKey(FIREBASE)
        fun firebaseTracker(tracker: FirebaseTracker): AnalyticsTracker
    }

    @Provides
    fun provideAnalytics(
        trackers: Map<String, @JvmSuppressWildcards AnalyticsTracker>
    ): Analytics = Analytics(trackers)

    @Provides
    fun provideAppMetric(): AppMetricTracker = AppMetricTrackerImpl()

    companion object Trackers {
        const val FIREBASE = "tracker_firebase"
    }
}
