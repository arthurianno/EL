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

class ReminderCallbackReceiver : BroadcastReceiver() {

    @Inject
    lateinit var bus: RxBus

    override fun onReceive(context: Context?, intent: Intent) {
        if (intent.action?.contains(ACTION_SPENT) == true) {
            AndroidInjection.inject(this, context)
            Timber.tag("Reminder").d("ACTION_SPENT")
            bus.event(Events.ReminderSpent(intent.getReminder()))
        }
    }
}