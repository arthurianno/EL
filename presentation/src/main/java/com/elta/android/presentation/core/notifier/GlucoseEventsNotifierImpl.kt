package com.elta.android.presentation.core.notifier

import com.elta.android.domain.features.devices.GlucoseEventsNotifier
import com.elta.android.presentation.Events
import com.elta.android.presentation.core.bus.event
import com.nullgr.core.rx.RxBus
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Реализация уведомителя о событиях глюкозы через RxBus.
 * Позволяет data слою уведомлять presentation слой об изменениях без нарушения Clean Architecture.
 */
@Singleton
class GlucoseEventsNotifierImpl @Inject constructor(
    private val bus: RxBus
) : GlucoseEventsNotifier {

    override fun notifyEventsChanged() {
        bus.event(Events.EventsChanged(true))
    }
}

