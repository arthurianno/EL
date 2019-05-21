package com.elta.android.presentation.di

import android.content.Context
import com.elta.android.presentation.analytics.core.Analytics
import com.elta.android.presentation.analytics.core.AnalyticsTracker
import com.elta.android.presentation.analytics.core.FirebaseTracker
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

@Module(includes = [AnalyticsModule.Declarations::class])
class AnalyticsModule(val context: Context) {

    @Module
    interface Declarations {
        @Binds
        @IntoMap
        @StringKey(FIREBASE)
        fun firebaseTracker(tracker: FirebaseTracker): AnalyticsTracker
    }

    @Provides
    fun analytics(
        trackers: Map<String, @JvmSuppressWildcards AnalyticsTracker>
    ): Analytics = Analytics(trackers)

    companion object Trackers {
        const val FIREBASE = "tracker_firebase"
    }
}