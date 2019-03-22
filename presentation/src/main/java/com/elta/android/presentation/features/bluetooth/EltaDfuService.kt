package com.elta.android.presentation.features.bluetooth

import android.app.Activity
import com.elta.android.presentation.BuildConfig
import com.elta.android.presentation.features.app.ui.AppActivity
import no.nordicsemi.android.dfu.DfuBaseService

class EltaDfuService : DfuBaseService() {
    override fun getNotificationTarget(): Class<out Activity> = AppActivity::class.java

    override fun isDebug(): Boolean = BuildConfig.DEBUG
}