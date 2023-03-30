package com.elta.android.common.logger

import java.io.File

class DebugTree(
    logsFile: File,
    private val enableLog: Boolean
) : BaseTree(logsFile) {

    override fun isLoggable(tag: String?, priority: Int): Boolean = enableLog
}
