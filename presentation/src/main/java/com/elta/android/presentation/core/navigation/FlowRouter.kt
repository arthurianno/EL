package com.elta.android.presentation.core.navigation

import ru.terrakok.cicerone.Router
import ru.terrakok.cicerone.android.support.SupportAppScreen

class FlowRouter(private val appRouter: Router) : UiThreadRouter() {

    fun startFlow(screen: SupportAppScreen) {
        appRouter.navigateTo(screen)
    }

    fun newRootFlow(screen: SupportAppScreen) {
        appRouter.newRootScreen(screen)
    }

    fun finishFlow() {
        appRouter.exit()
    }

    fun replaceFlow(screen: SupportAppScreen) {
        appRouter.replaceScreen(screen)
    }
}
