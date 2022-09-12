package com.elta.android.common.logger.model

data class FirebaseLogRecord(
    val time: String,
    val priority: String,
    val tag: String?,
    val message: String,
    val t: String?
)
