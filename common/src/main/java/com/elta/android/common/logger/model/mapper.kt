package com.elta.android.common.logger.model

import android.util.Log

internal fun LogRecord.toFirebase(): FirebaseLogRecord =
    FirebaseLogRecord(
        time = time,
        priority = priorityAsString(priority),
        tag = tag,
        message = message,
        t = t?.toString()
    )

internal fun priorityAsString(priority: Int): String = when (priority) {
    Log.VERBOSE -> "VERBOSE"
    Log.DEBUG -> "DEBUG"
    Log.INFO -> "INFO"
    Log.WARN -> "WARN"
    Log.ERROR -> "ERROR"
    Log.ASSERT -> "ASSERT"
    else -> priority.toString()
}
