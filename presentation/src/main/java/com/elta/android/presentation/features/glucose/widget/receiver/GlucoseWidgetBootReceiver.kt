package com.elta.android.presentation.features.glucose.widget.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.elta.android.presentation.features.glucose.widget.worker.GlucoseWidgetUpdateWorker
import timber.log.Timber

/**
 * Reschedules widget updates after reboot/app update.
 */
class GlucoseWidgetBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val safeContext = context ?: return
        val action = intent?.action ?: return

        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            runCatching {
                GlucoseWidgetUpdateWorker.schedulePeriodic(safeContext)
                GlucoseWidgetUpdateWorker.requestImmediateUpdate(safeContext)
            }.onFailure { Timber.e(it, "Failed to reschedule glucose widget worker") }
        }
    }
}
