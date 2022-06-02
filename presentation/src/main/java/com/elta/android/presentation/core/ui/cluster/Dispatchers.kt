package com.elta.android.presentation.core.ui.cluster

import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

object Dispatchers {
    val Map = Executors.newFixedThreadPool(1).asCoroutineDispatcher()
    val Main = kotlinx.coroutines.Dispatchers.Main
}
