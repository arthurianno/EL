package com.elta.android.common.logger

import android.os.Environment
import android.util.Log
import timber.log.Timber
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

abstract class BaseTree : Timber.Tree() {
    companion object {
        const val DEFAULT_TAG = "ELTA_LOG_TAG"
    }

    private val logsFile by lazy {
        File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "EltaApplicationLog_" + SimpleDateFormat("dd-M-yyyy").format(Date()) + ".txt"
        ).apply {
            if (!exists()) {
                try {
                    createNewFile()
                } catch (e: IOException) {
                    Log.e(DEFAULT_TAG, e.message, e)
                }
            }
        }
    }

    private val _logs = mutableListOf<LogRecord>()
    val logs: List<LogRecord>
        get() = _logs

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        _logs.add(LogRecord(Date().time, priority, tag, message, t))
    }

    protected fun saveLogInFile(logRecord: LogRecord) {
        var buf: BufferedWriter? = null
        try {
            buf = BufferedWriter(FileWriter(logsFile, true))
            buf writeLog logRecord
        } catch (e: IOException) {
            Log.e(DEFAULT_TAG, e.message, e)
        } finally {
            buf?.close()
        }
    }

    private infix fun BufferedWriter.writeLog(logRecord: LogRecord) {
        append(" [" + SimpleDateFormat("dd-M-yyyy hh:mm:ss").format(logRecord.time) + "] --> ")
        when (logRecord.priority) {
            Log.INFO -> append("I/")
            Log.ASSERT -> append("A/")
            Log.DEBUG -> append("D/")
            Log.VERBOSE -> append("V/")
            Log.WARN -> append("W/")
            Log.ERROR -> append("E/")
        }
        append("${logRecord.tag ?: DEFAULT_TAG}: ${logRecord.message}")
        newLine()
    }
}
