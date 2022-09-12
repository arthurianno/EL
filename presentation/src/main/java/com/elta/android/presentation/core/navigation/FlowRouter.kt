package com.elta.android.presentation.core.navigation

import com.elta.android.presentation.core.navigation.commands.AddTabs
import com.elta.android.presentation.core.navigation.commands.AttachTab
import com.elta.android.presentation.core.navigation.support.SupportAppScreen
import com.github.terrakok.cicerone.Router

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

    fun newTabs(screens: Array<SupportAppScreen>) {
        executeCommands(AddTabs(screens))
    }

    fun navigateToTab(screen: SupportAppScreen) {
        executeCommands(AttachTab(screen))
    }

    private fun runCommand(command: Router.() -> Unit) {
        if (parentRouter != null)
            parentRouter.command()
        else
            command()
    }
}
