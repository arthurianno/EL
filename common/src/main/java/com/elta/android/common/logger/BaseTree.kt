package com.elta.android.common.logger

import android.os.Environment
import android.util.Log
import com.elta.android.common.logger.model.DeviceDetails
import com.elta.android.common.logger.model.LogRecord
import com.elta.android.common.logger.model.priorityAsString
import com.elta.android.common.logger.model.toFirebase
import com.google.firebase.database.FirebaseDatabase
import timber.log.Timber
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal const val DEFAULT_TAG = "ELTA_LOG_TAG"

abstract class BaseTree(private val deviceDetails: DeviceDetails) : Timber.Tree() {

    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS a zzz", Locale.getDefault())
    private val date = dateFormat.format(Date())
    private val firebaseRef =
        FirebaseDatabase.getInstance().getReference("logs/$date/${deviceDetails.deviceId}")

    private val logsFile by lazy {
        File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "EltaApplicationLog_$date.txt"
        ).apply {
            if (!exists()) {
                runCatching { createNewFile() }
                    .onFailure { Log.e(DEFAULT_TAG, it.message, it) }
            }
        }
    }

    private val _logs = mutableListOf<LogRecord>()
    val logs: List<LogRecord>
        get() = _logs

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        _logs.add(LogRecord(timeFormat.format(Date()), priority, tag, message, t))
    }

    protected fun storeToFirebase(logRecord: LogRecord) {
        val timestamp = System.currentTimeMillis()
        with(firebaseRef) {
            updateChildren(mapOf(Pair("-DeviceDetails", deviceDetails)))
            child(timestamp.toString()).setValue(logRecord.toFirebase())
        }
    }

    protected fun saveLogInFile(logRecord: LogRecord) {
        BufferedWriter(FileWriter(logsFile, true)).also { file ->
            runCatching { file writeLog logRecord }
                .onFailure { Log.e(DEFAULT_TAG, it.message, it) }
            file.close()
        }
    }

    private infix fun BufferedWriter.writeLog(logRecord: LogRecord) {
        append("[${logRecord.time}] --> ")
        append("${priorityAsString(logRecord.priority)}/")
        append("${logRecord.tag ?: DEFAULT_TAG}: ${logRecord.message}")
        newLine()
    }
}
