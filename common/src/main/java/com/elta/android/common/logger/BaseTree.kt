package com.elta.android.common.logger

import android.util.Log
import com.elta.android.common.logger.model.LogRecord
import timber.log.Timber
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal const val DEFAULT_TAG = "ELTA_LOG_TAG"
private const val MAX_FILE_SIZE_BYTES = 2 * 1024 * 1024L // 2 MB

abstract class BaseTree(private val logsFile: File) : Timber.Tree() {

    private val timeFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS a zzz", Locale.getDefault())

    private val logs = mutableListOf<LogRecord>()

    private var lastLogMessage: String? = null
    private var lastLogTime: Long = 0
    private var repeatCount = 0

    override fun log(priority: Int, tag: String?, message: String, error: Throwable?) {
        val maskedMessage = LogMasker.mask(message)

        // Duplicate suppression: if same message is logged within 1.5 seconds, increment repeatCount
        if (maskedMessage == lastLogMessage && (System.currentTimeMillis() - lastLogTime) < 1500L) {
            repeatCount++
            return
        } else {
            if (repeatCount > 0) {
                writeSystemMessage("Last message repeated $repeatCount times")
                repeatCount = 0
            }
            lastLogMessage = maskedMessage
            lastLogTime = System.currentTimeMillis()
        }

        Log.println(priority, tag, maskedMessage)
        val logRecord = LogRecord(timeFormat.format(Date()), priority, tag, maskedMessage, error)
        logs.add(logRecord)

        try {
            checkAndRotateFile(logsFile)
            saveLogInFile(logRecord)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun writeSystemMessage(msg: String) {
        try {
            val systemRecord = LogRecord(timeFormat.format(Date()), Log.INFO, "SYSTEM", msg, null)
            saveLogInFile(systemRecord)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkAndRotateFile(file: File) {
        if (!file.exists()) return
        if (file.length() > MAX_FILE_SIZE_BYTES) {
            try {
                val parentDir = file.parentFile ?: return
                val baseName = file.nameWithoutExtension
                val ext = file.extension
                var index = 1
                var backupFile = File(parentDir, "$baseName.$index.$ext")
                while (backupFile.exists()) {
                    index++
                    backupFile = File(parentDir, "$baseName.$index.$ext")
                }
                
                // Copy current logs to backupFile and truncate current logsFile
                file.copyTo(backupFile, overwrite = true)
                FileWriter(file, false).use { writer ->
                    writer.write("[SYSTEM]: Log rotated. Previous logs backed up to ${backupFile.name}\n")
                }
            } catch (e: Exception) {
                Log.e("BaseTree", "Failed to rotate log file", e)
            }
        }
    }

    private fun saveLogInFile(logRecord: LogRecord) {
        BufferedWriter(FileWriter(logsFile, true)).use { file ->
            runCatching { file writeLog logRecord }
                .onFailure { Log.println(Log.ERROR, DEFAULT_TAG, it.message.orEmpty()) }
        }
    }

    private infix fun BufferedWriter.writeLog(logRecord: LogRecord) {
        append("[${logRecord.time}] --> ")
        append(logRecord.priority.priorityToString())
        append(" ${logRecord.tag ?: DEFAULT_TAG}: ${logRecord.message}")
        newLine()
    }

    private fun Int.priorityToString(): String = when (this) {
        Log.VERBOSE -> "VERBOSE"
        Log.DEBUG -> "DEBUG"
        Log.INFO -> "INFO"
        Log.WARN -> "WARN"
        Log.ERROR -> "ERROR"
        Log.ASSERT -> "ASSERT"
        else -> super.toString()
    }
}

