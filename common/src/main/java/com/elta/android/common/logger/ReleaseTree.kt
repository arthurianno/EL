package com.elta.android.common.logger

import android.util.Log

class ReleaseTree(deviceDetails: DeviceDetails) : BaseTree(deviceDetails) {

    override fun isLoggable(tag: String?, priority: Int): Boolean = priority == Log.INFO
}
