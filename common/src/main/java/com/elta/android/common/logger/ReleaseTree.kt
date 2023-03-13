package com.elta.android.common.logger

import android.content.Context
import android.util.Log
import com.elta.android.common.logger.model.DeviceDetails

class ReleaseTree(deviceDetails: DeviceDetails, context: Context) :
    BaseTree(deviceDetails, context) {

    override fun isLoggable(tag: String?, priority: Int): Boolean = priority == Log.INFO
}
