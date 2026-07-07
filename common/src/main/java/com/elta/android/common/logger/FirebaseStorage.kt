package com.elta.android.common.logger

import android.content.Context
import android.net.Uri
import android.provider.Settings
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.elta.android.common.logger.model.DeviceDetails
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val FIREBASE_STORAGE_PATH_KEY = "firebase_storage_path"
private const val LOCAL_FILE_URI_KEY = "local_file_uri"
private const val UPLOAD_ERROR_LOG_MESSAGE = "Upload log file is failure"

class FirebaseStorage(private val context: Context) {

    var userLogin: String? = null
        get() = field ?: "Not authorization"

    val localLogsFile by lazy {
        File(context.getExternalFilesDir("logs"), "EltaApplicationLog_$date.txt").apply {
            cleanOldLogs()
            if (!exists()) {
                runCatching { createNewFile() }
                    .onFailure { Timber.tag(DEFAULT_TAG).e(it) }
            }
        }
    }

    private val deviceDetails = DeviceDetails(
        Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
    )

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val date = dateFormat.format(Date())

    fun cleanOldLogs() {
        try {
            val logsDir = context.getExternalFilesDir("logs") ?: return
            if (!logsDir.exists() || !logsDir.isDirectory) return
            val files = logsDir.listFiles() ?: return

            // Get all files matching EltaApplicationLog_*.txt
            val logFiles = files.filter { it.isFile && it.name.startsWith("EltaApplicationLog_") && it.name.endsWith(".txt") }
                .sortedBy { it.lastModified() }

            // 1. Delete files older than 5 days
            val fiveDaysAgo = System.currentTimeMillis() - (5 * 24 * 60 * 60 * 1000L)
            for (file in logFiles) {
                if (file.lastModified() < fiveDaysAgo) {
                    file.delete()
                }
            }

            // 2. Control total logs folder size (max 10 MB)
            val remainingFiles = logsDir.listFiles()
                ?.filter { it.isFile && it.name.startsWith("EltaApplicationLog_") && it.name.endsWith(".txt") }
                ?.sortedBy { it.lastModified() } ?: return

            var totalSize = remainingFiles.sumOf { it.length() }
            val maxTotalSize = 10 * 1024 * 1024L // 10 MB

            val iterator = remainingFiles.iterator()
            while (totalSize > maxTotalSize && iterator.hasNext()) {
                val oldestFile = iterator.next()
                val size = oldestFile.length()
                if (oldestFile.delete()) {
                    totalSize -= size
                }
            }
        } catch (e: Exception) {
            Timber.tag(DEFAULT_TAG).e(e, "Error cleaning old logs")
        }
    }


    fun uploadLogFile() {
        WorkManager.getInstance(context).enqueue(
            OneTimeWorkRequestBuilder<UploadWorker>()
                .setInputData(getUploadWorkerData())
                .build()
        )
    }

    private fun getUploadWorkerData(): Data {
        val storagePath =
            "android_logs/$date/${deviceDetails.manufacturer}-${deviceDetails.model}-Android${deviceDetails.osVersion}-${deviceDetails.deviceId}-$userLogin-${deviceDetails.appVersion}.txt"
        return Data.Builder()
            .putString(FIREBASE_STORAGE_PATH_KEY, storagePath)
            .putString(LOCAL_FILE_URI_KEY, Uri.fromFile(localLogsFile).toString())
            .build()
    }
}

internal class UploadWorker(context: Context, params: WorkerParameters) :
    Worker(context, params) {
    override fun doWork(): Result {
        val storagePath = inputData.getString(FIREBASE_STORAGE_PATH_KEY).orEmpty()
        val localLogsFileUri = Uri.parse(inputData.getString(LOCAL_FILE_URI_KEY))
        return try {
            Firebase.storage.reference.child(storagePath)
                .putFile(localLogsFileUri)
                .addOnFailureListener { Timber.e(it, UPLOAD_ERROR_LOG_MESSAGE) }
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, UPLOAD_ERROR_LOG_MESSAGE)
            Result.failure()
        }
    }
}
