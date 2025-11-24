package com.elta.android.data.features.devices.glucometer.service.firmware

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.elta.android.common.di.qualifires.Firmware
import com.elta.android.common.di.qualifires.UpdateType
import com.elta.android.common.errors.CommandStillWritingError
import com.elta.android.common.logger.crashlyrics.CrashlyticsReport
import com.elta.android.data.R
import com.elta.android.data.features.devices.glucometer.client.FirmwareChunk
import com.elta.android.data.features.devices.glucometer.client.FirmwareChunk.Companion.CMD_BIN_CODE
import com.elta.android.data.features.devices.glucometer.client.FirmwareChunk.Companion.CMD_DAT_CODE
import com.elta.android.data.features.devices.glucometer.client.FirmwareChunk.Companion.MAX_DATA_BYTES_IN_CHUNK
import com.elta.android.data.features.devices.glucometer.client.FirmwareChunk.Companion.NUM_DAT
import com.elta.android.data.features.devices.glucometer.client.FirmwareChunk.Companion.START_ADDRESS
import com.elta.android.data.features.devices.glucometer.client.GlucometerClientImpl
import com.elta.android.data.features.firmware.datasource.FirmwaresManager
import com.elta.android.domain.features.devices.CONNECT_TIMEOUT
import com.elta.android.domain.features.devices.model.BootModeStatus
import dagger.android.DaggerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.io.File
import java.util.concurrent.CancellationException
import javax.inject.Inject

class BootModeService : DaggerService() {
    companion object {
        const val ADDRESS_EXTRA_KEY = "address_key"
        const val FILE_PATH_EXTRA_KEY = "file_path_key"
        const val PIN_EXTRA_KEY = "pin_key"
    }

    @Inject
    @Firmware(UpdateType.BootMode)
    lateinit var glucometerClient: GlucometerClientImpl

    @Inject
    lateinit var crashlyticsReport: CrashlyticsReport

    @Inject
    lateinit var fileManager: FirmwaresManager

    private var job = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + job)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(Service.STOP_FOREGROUND_REMOVE)
        job.cancel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(FOREGROUND_ID, getNotification())
        coroutineScope.launch(Dispatchers.IO) {
            val address = intent?.getStringExtra(ADDRESS_EXTRA_KEY).orEmpty()
            val pin = intent?.getStringExtra(PIN_EXTRA_KEY).orEmpty()
            val filePath = intent?.getStringExtra(FILE_PATH_EXTRA_KEY).orEmpty()

            try {
                val (binFile, datFile) = unzipBinAndConfigFile(filePath)

                val binFileBytesArray = binFile.readBytes()
                val datFileBytesArray = datFile.readBytes()

                withTimeout(CONNECT_TIMEOUT) {
                    glucometerClient.connectDevice(address, pin)
                }
                glucometerClient.turnOnDfuMode()

                sendBytesChunk(binFileBytesArray, FileType.Bin)
                sendBytesChunk(datFileBytesArray, FileType.Dat)

                receiveStatus(BootModeStatus.Completed)

            } catch (ex: Exception) {
                val errorStatus = when (ex) {
                    is CancellationException -> BootModeStatus.SyncFailed
                    else -> BootModeStatus.UpdateFailed
                }
                crashlyticsReport.writeException(ex)
                receiveStatus(errorStatus)
                stopForeground(Service.STOP_FOREGROUND_REMOVE)
            }

            stopForeground(Service.STOP_FOREGROUND_REMOVE)
        }

        return START_NOT_STICKY
    }

    private suspend fun sendBytesChunk(fileBytes: ByteArray, fileType: FileType) {
        receiveStatus(BootModeStatus.Progress)
        if (fileBytes.size % 4 != 0) throw Exception("File must division by 4")

        val numberOfFullChunk = fileBytes.size / MAX_DATA_BYTES_IN_CHUNK
        val sizeOfEndChunk = fileBytes.size % MAX_DATA_BYTES_IN_CHUNK

        val chunkCount = if (fileType == FileType.Bin) numberOfFullChunk + 1 else 1
        val command = when (fileType) {
            FileType.Bin -> CMD_BIN_CODE
            FileType.Dat -> CMD_DAT_CODE
        }.toByte()

        var address = START_ADDRESS
        var index = 0

        for (counter in 1..chunkCount) {
            updateNotificationBar(counter, chunkCount)

            receiveStatus(BootModeStatus.Progress)
            crashlyticsReport.log("Send $counter $fileType-chunk of $chunkCount")

            val dataChunk =
                fileBytes.copyOfRange(index, minOf(index + MAX_DATA_BYTES_IN_CHUNK, fileBytes.size))

            val number = when (fileType) {
                FileType.Bin -> if (counter == chunkCount) sizeOfEndChunk else MAX_DATA_BYTES_IN_CHUNK
                FileType.Dat -> NUM_DAT
            }

            val chunk = FirmwareChunk(
                cmd = command,
                adr = address,
                num = number.toUByte(),
                data = dataChunk
            )

            handleChunkSending(chunk)

            index += MAX_DATA_BYTES_IN_CHUNK
            address += when (fileType) {
                FileType.Bin -> MAX_DATA_BYTES_IN_CHUNK
                FileType.Dat -> START_ADDRESS
            }
        }
    }

    private fun updateNotificationBar(counter: Int, chunkCount: Int) {
        val progress = calculateProgress(counter, chunkCount)
        val notification = getNotification(progress)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(this).notify(FOREGROUND_ID, notification)
        }
    }

    /**
     * Отправляет массив байтов. Если придёт ошибка CommandStillWritingError, то повторяем отправку один раз.
     * Если ошибка повториться, или придёт любая другая ошибка, то пробрасываем её и останавливаем обновление.
     */
    private suspend fun handleChunkSending(chunk: FirmwareChunk, isSecondSending: Boolean = false) {
        try {
            glucometerClient.sendFirmwareChunk(chunk)
        } catch (ex: Exception) {
            when {
                ex is CommandStillWritingError && !isSecondSending ->
                    handleChunkSending(chunk = chunk, isSecondSending = true)

                else -> {
                    receiveStatus(BootModeStatus.UpdateFailed)
                    throw ex
                }
            }
        }
    }

    private fun unzipBinAndConfigFile(filePath: String): Pair<File, File> {
        val filesPath = fileManager.unpackZip(filePath)

        if (filesPath.size != 2) throw Exception("Not enough files for updating")

        val binFile =
            fileManager.getFile(name = filesPath.first(), isZipFile = false)
                ?: throw Exception("Not found bin file")
        val datFile =
            fileManager.getFile(name = filesPath.last(), isZipFile = false)
                ?: throw Exception("Not found dat file")

        return binFile to datFile
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, "Firmware update", NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun getNotification(progress: Int = 0): Notification {
        val builder =
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Firmware update")
                .setSmallIcon(R.drawable.ic_notification_reminder)
                .setProgress(100, progress, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
        }
        return builder.build()
    }

    private fun receiveStatus(status: BootModeStatus) {
        val intent = Intent(BootModeStatus.ACTION_STATUS_NAME)
        intent.putExtra(BootModeStatus.STATUS_NAME_KEY, status.name)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

    private fun calculateProgress(start: Int, end: Int): Int = start * 100 / end

    private enum class FileType {
        Bin, Dat
    }
}

private const val CHANNEL_ID = "123"
private const val FOREGROUND_ID = 1
