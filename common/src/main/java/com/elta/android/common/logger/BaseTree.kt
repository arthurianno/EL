package com.elta.android.common.logger

import android.util.Log
import com.elta.android.common.logger.model.LogRecord
import com.elta.android.common.logger.model.priorityAsString
import timber.log.Timber
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal const val DEFAULT_TAG = "ELTA_LOG_TAG"

abstract class BaseTree(private val logsFile: File) : Timber.Tree() {

    private val timeFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS a zzz", Locale.getDefault())

    private val _logs = mutableListOf<LogRecord>()
    val logs: List<LogRecord>
        get() = _logs

    override fun log(priority: Int, tag: String?, message: String, error: Throwable?) {
        val logRecord = LogRecord(timeFormat.format(Date()), priority, tag, message, error)
        Log.println(priority, tag, message)
        _logs.add(logRecord)
        saveLogInFile(logRecord)
    }

    private fun saveLogInFile(logRecord: LogRecord) {
        BufferedWriter(FileWriter(logsFile, true)).use { file ->
            runCatching { file writeLog logRecord }
                .onFailure { Log.println(Log.ERROR, DEFAULT_TAG, it.message.orEmpty()) }
        }
    }

    private infix fun BufferedWriter.writeLog(logRecord: LogRecord) {
        append("[${logRecord.time}] --> ")
        append("${priorityAsString(logRecord.priority)}/")
        append("${logRecord.tag ?: DEFAULT_TAG}: ${logRecord.message}")
        newLine()
    }
}
