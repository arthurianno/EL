package com.elta.android.presentation.core.navigation

import ru.terrakok.cicerone.Router
import ru.terrakok.cicerone.android.support.SupportAppScreen

class FlowRouter(private val parentRouter: Router?) : UiThreadRouter() {

    fun startFlow(screen: SupportAppScreen) {
        runCommand { navigateTo(screen) }
    }

    fun newRootFlow(screen: SupportAppScreen) {
        runCommand { newRootScreen(screen) }
    }

    fun finishFlow() {
        runCommand { exit() }
    }

    fun replaceFlow(screen: SupportAppScreen) {
        // TODO impelement attach/detach command
        runCommand { replaceScreen(screen) }
    }

    private fun runCommand(command: Router.() -> Unit) {
        if (parentRouter != null)
            parentRouter.command()
        else
            this.command()
    }
}
