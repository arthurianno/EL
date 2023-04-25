package com.elta.android.common.logger

import android.util.Log
import java.io.File

class ReleaseTree(
    logsFile: File,
    private val enableLog: Boolean
) : BaseTree(logsFile) {
    override fun isLoggable(tag: String?, priority: Int): Boolean =
        enableLog || priority == Log.INFO || priority == Log.ERROR
}
