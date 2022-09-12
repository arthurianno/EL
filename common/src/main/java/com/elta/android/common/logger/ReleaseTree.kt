package com.elta.android.common.logger

import android.util.Log
import com.elta.android.common.logger.model.DeviceDetails

class ReleaseTree(deviceDetails: DeviceDetails) : BaseTree(deviceDetails) {

    override fun isLoggable(tag: String?, priority: Int): Boolean = priority == Log.INFO
}
