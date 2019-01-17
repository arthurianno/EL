@file:Suppress("UNCHECKED_CAST")

package com.elta.android.presentation.core.navigation

import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.Router

class LocalCiceroneHolder {

    private val containers = hashMapOf<String, Cicerone<Router>>()

    operator fun get(containerTag: String): Cicerone<Router> =
        containers[containerTag] ?: Cicerone.create(UiThreadRouter()).apply {
            containers[containerTag] = this as Cicerone<Router>
        } as Cicerone<Router>
}