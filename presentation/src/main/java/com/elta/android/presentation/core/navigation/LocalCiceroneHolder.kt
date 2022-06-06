@file:Suppress("UNCHECKED_CAST")

package com.elta.android.presentation.core.navigation

import com.github.terrakok.cicerone.Cicerone
import com.github.terrakok.cicerone.Router

class LocalCiceroneHolder {

    private val containers = hashMapOf<String, Cicerone<Router>>()

    operator fun get(containerTag: String): Cicerone<Router> =
        containers[containerTag] ?: Cicerone.create(UiThreadRouter()).apply {
            containers[containerTag] = this as Cicerone<Router>
        } as Cicerone<Router>
}
