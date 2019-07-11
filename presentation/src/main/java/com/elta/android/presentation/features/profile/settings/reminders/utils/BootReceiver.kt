package com.elta.android.presentation.features.profile.settings.reminders.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.elta.android.presentation.Events
import com.elta.android.presentation.core.bus.event
import com.nullgr.core.rx.RxBus
import dagger.android.AndroidInjection
import javax.inject.Inject

class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var bus: RxBus

    private val bootAction: String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            Intent.ACTION_LOCKED_BOOT_COMPLETED
        else
            Intent.ACTION_BOOT_COMPLETED

    override fun onReceive(context: Context?, intent: Intent) {
        AndroidInjection.inject(this, context)
        val action = intent.action
        if (action == bootAction) {
            bus.event(Events.BootCompleted)
        }
    }
}