package com.elta.android.common.logger

import android.content.Context
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.elta.android.common.logger.model.DeviceDetails
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.text.SimpleDateFormat
import java.time.Duration
import java.util.Date
import java.util.Locale

private const val FIREBASE_STORAGE_PATH_KEY = "firebase_storage_path"
private const val LOCAL_FILE_URI_KEY = "local_file_uri"

class FirebaseStorage(private val context: Context) {

    var userLogin: String? = null
        get() = field ?: "Not authorization"

    val localLogsFile by lazy {
        File(context.getExternalFilesDir("logs"), "EltaApplicationLog_$date.txt").apply {
            if (!exists()) {
                runCatching { createNewFile() }
                    .onFailure { Log.e(DEFAULT_TAG, it.message, it) }
            }
        }
    }

    private val deviceDetails = DeviceDetails(
        Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
    )

    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    private val date = dateFormat.format(Date())

    fun uploadLogFile() {
        WorkManager.getInstance(context).enqueue(
            OneTimeWorkRequestBuilder<UploadWorker>()
                .setInputData(getUploadWorkerData())
                .build()
        )
    }

    fun createPeriodicUploadLogFile(repeatDuration: Duration) {
        WorkManager.getInstance(context).enqueue(
            PeriodicWorkRequestBuilder<UploadWorker>(repeatDuration)
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
                .addOnFailureListener { throw Exception() }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
