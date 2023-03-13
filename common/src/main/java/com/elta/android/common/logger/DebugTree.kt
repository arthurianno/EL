package com.elta.android.common.logger

import android.content.Context
import android.util.Log
import com.elta.android.common.logger.model.DeviceDetails

class DebugTree(deviceDetails: DeviceDetails, context: Context) : BaseTree(deviceDetails, context) {

    override fun log(priority: Int, tag: String?, message: String, error: Throwable?) {
        super.log(priority, tag, message, error)
        val localTag = tag ?: DEFAULT_TAG
        val logRecord = logs.last()
        saveLogInFile(logRecord)
        storeToFirebase(logRecord)
        error?.let {
            Log.e(localTag, message, error)
        } ?: Log.println(priority, localTag, message)
    }
}
