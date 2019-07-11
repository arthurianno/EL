package com.elta.android.presentation.core.date

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.elta.android.presentation.core.bus.event
import com.nullgr.core.rx.RxBus
import dagger.android.AndroidInjection
import javax.inject.Inject

class DateChangeReceiver : BroadcastReceiver() {

    @Inject
    lateinit var bus: RxBus

    override fun onReceive(context: Context?, intent: Intent?) {
        AndroidInjection.inject(this, context)
        bus.event(DateChangedEvent)
    }
}