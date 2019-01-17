package com.elta.android.presentation.core.navigation

import android.os.Handler
import ru.terrakok.cicerone.Router
import ru.terrakok.cicerone.commands.Command

open class UiThreadRouter : Router() {

    private val handler = Handler()

    @Suppress("SpreadOperator")
    override fun executeCommands(vararg commands: Command?) {
        handler.post {
            super.executeCommands(*commands)
        }
    }
}