package com.elta.android.presentation.features.profile.settings.reminders.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.elta.android.presentation.jobs.RemindersManager
import dagger.android.AndroidInjection
import timber.log.Timber
import javax.inject.Inject

class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var remindersManager: RemindersManager

    override fun onReceive(context: Context?, intent: Intent?) {
        AndroidInjection.inject(this, context)
        Timber.tag("Reminder").d("receiver: Events.BootCompleted")
        remindersManager.bootComplete()
    }
}