package com.elta.android.presentation.features.home.pm

import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.GlucoseInputType
import com.elta.android.presentation.R
import com.elta.android.presentation.Screens
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEventType
import com.elta.android.presentation.core.navigation.FlowRouter
import com.elta.android.presentation.core.navigation.support.SupportAppScreen

internal class HomeFlowNavigator(private val router: FlowRouter) {
    fun showTabs() {
        router.newTabs(
            arrayOf(Screens.MainTab, Screens.DiaryTab, Screens.StatisticTab, Screens.ProfileTab)
        )
        router.navigateToTab(Screens.MainTab)
    }

    fun tabForMenu(id: Int): Pair<SupportAppScreen, String> =
        when (id) {
            R.id.mainMenuItemView -> Screens.MainTab to AnalyticsEventType.HOMEPAGE
            R.id.notesMenuItemView -> Screens.DiaryTab to AnalyticsEventType.DIARY_OPEN
            R.id.statsMenuItemView -> Screens.StatisticTab to AnalyticsEventType.STATISTICS_OPEN
            R.id.profileMenuItemView -> Screens.ProfileTab to AnalyticsEventType.PROFILE_OPEN
            else -> throw IllegalArgumentException("$id is not supported.")
        }

    fun openTab(screen: SupportAppScreen) {
        router.navigateToTab(screen)
    }

    fun openEvent(event: EventType) {
        router.startFlow(Screens.EventsCreationScreen(event))
    }

    fun openManualGlucose() {
        openEvent(EventType.Glucose(GlucoseInputType.MANUAL))
    }

    fun openConnectDevice() {
        router.startFlow(Screens.ConnectStartScreen(isOnBoarding = false))
    }

    fun navigateToFeedback() {
        router.navigateTo(Screens.Feedback)
    }

    fun startFeedbackFlow() {
        router.startFlow(Screens.Feedback)
    }

    fun openPlayMarket() {
        router.navigateTo(Screens.PlayMarketScreen)
    }
}
