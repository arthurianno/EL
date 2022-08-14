package com.elta.android.common.logger

import android.util.Log
import com.elta.android.common.logger.model.DeviceDetails

class DebugTree(deviceDetails: DeviceDetails) : BaseTree(deviceDetails) {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        super.log(priority, tag, message, t)
        val localTag = tag ?: DEFAULT_TAG
        val logRecord = logs.last()
        saveLogInFile(logRecord)
        storeToFirebase(logRecord)
        t?.let {
            Log.e(localTag, message, t)
        } ?: Log.println(priority, localTag, message)
    }
}
