package com.elta.android.presentation.features.profile.settings.reminders.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.elta.android.presentation.Events
import com.elta.android.presentation.core.bus.event
import com.nullgr.core.rx.RxBus
import dagger.android.AndroidInjection
import timber.log.Timber
import javax.inject.Inject

class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var bus: RxBus

    override fun onReceive(context: Context?, intent: Intent?) {
        AndroidInjection.inject(this, context)
        Timber.d("receiver: Events.BootCompleted")
        bus.event(Events.BootCompleted)
    }
}