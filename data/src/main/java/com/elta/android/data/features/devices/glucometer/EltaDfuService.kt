package com.elta.android.data.features.devices.glucometer

import android.app.Activity
import com.elta.android.data.BuildConfig
import no.nordicsemi.android.dfu.DfuBaseService

class EltaDfuService : DfuBaseService() {
    override fun getNotificationTarget(): Class<out Activity>? = Class.forName("com.elta.android.presentation.features.app.ui.AppActivity") as Class<Activity>

    override fun isDebug(): Boolean = BuildConfig.DEBUG
}