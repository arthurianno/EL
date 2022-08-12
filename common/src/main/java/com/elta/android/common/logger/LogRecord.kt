package com.elta.android.common.logger

data class LogRecord(
    val time: Long,
    val priority: Int,
    val tag: String?,
    val message: String,
    val t: Throwable?
)
