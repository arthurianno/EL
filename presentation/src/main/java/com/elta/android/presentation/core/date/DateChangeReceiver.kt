package com.elta.android.presentation.core.date

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.elta.android.presentation.core.bus.event
import com.nullgr.core.rx.RxBus
import org.threeten.bp.ZonedDateTime

class DateChangeReceiver(private val bus: RxBus) : BroadcastReceiver() {

    private var previousDate = ZonedDateTime.now().withSecond(0).withNano(0)
    private var currentDate = ZonedDateTime.from(previousDate)

    override fun onReceive(context: Context?, intent: Intent?) {
        val newDate = ZonedDateTime.now().withSecond(0).withNano(0)
        if (currentDate != newDate) {
            previousDate = currentDate
            currentDate = newDate
            bus.event(DateChangedEvent(previousDate, currentDate))
        }
    }

    fun register(context: Context) {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }

        context.registerReceiver(this, filter)
    }
}