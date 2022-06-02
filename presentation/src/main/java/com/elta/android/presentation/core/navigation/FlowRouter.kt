package com.elta.android.presentation.core.navigation

import com.elta.android.presentation.core.navigation.commands.AddTabs
import com.elta.android.presentation.core.navigation.commands.AttachTab
import com.github.terrakok.cicerone.Router
import com.github.terrakok.cicerone.Screen

class FlowRouter(private val parentRouter: Router?) : UiThreadRouter() {

    fun startFlow(screen: Screen) {
        runCommand { navigateTo(screen) }
    }

    fun newRootFlow(screen: Screen) {
        runCommand { newRootScreen(screen) }
    }

    fun finishFlow() {
        runCommand { exit() }
    }

    fun newTabs(screens: Array<Screen>) {
        executeCommands(AddTabs(screens))
    }

    fun navigateToTab(screen: Screen) {
        executeCommands(AttachTab(screen))
    }

    private fun runCommand(command: Router.() -> Unit) {
        if (parentRouter != null)
            parentRouter.command()
        else
            command()
    }
}
