package com.elta.android.common.logger.model

internal data class LogRecord(
    val time: String,
    val priority: Int,
    val tag: String?,
    val message: String,
    val t: Throwable?
)
