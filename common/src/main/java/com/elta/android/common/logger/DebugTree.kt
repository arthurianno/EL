package com.elta.android.common.logger

import android.util.Log

class DebugTree : BaseTree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        super.log(priority, tag, message, t)
        val localTag = tag ?: DEFAULT_TAG
        saveLogInFile(logs.last())
        t?.let {
            Log.e(localTag, message, t)
        } ?: Log.println(priority, localTag, message)
    }
}
