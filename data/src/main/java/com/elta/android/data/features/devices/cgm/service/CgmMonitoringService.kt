package com.elta.android.data.features.devices.cgm.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.elta.android.data.features.devices.cgm.client.NmgGattInspector
import com.elta.android.data.features.devices.cgm.protocol.NmgAdvertisementParser
import com.elta.android.data.features.devices.cgm.storage.NmgMeasurementStore
import com.elta.android.data.features.devices.cgm.storage.NmgMonitoringPreferences
import dagger.android.DaggerService
import javax.inject.Inject

/**
 * Keeps receiving advertising packets from the one explicitly selected NMG sensor.
 * It does not keep a GATT connection open: the documented live data transport is advertising.
 */
class CgmMonitoringService : DaggerService() {
    @Inject lateinit var bluetoothAdapter: BluetoothAdapter
    @Inject lateinit var measurementStore: NmgMeasurementStore
    @Inject lateinit var preferences: NmgMonitoringPreferences
    @Inject lateinit var gattInspector: NmgGattInspector

    private val handler = Handler(Looper.getMainLooper())
    private var isScanning = false
    private var scanStartedAtEpochMillis = 0L
    private var lastScanRestartAtEpochMillis = 0L
    private var connectionStatus = ConnectionStatus.WAITING

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            handleScanResult(result)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            results.forEach(::handleScanResult)
        }

        override fun onScanFailed(errorCode: Int) {
            isScanning = false
            Log.e(TAG, "NMG_SCAN_FAILED code=$errorCode")
            scheduleScanRestart("scan_failed_$errorCode")
        }
    }

    private val scanRestart = Runnable {
        if (!preferences.activeSensorId.isNullOrBlank()) {
            startScanning()
        }
    }

    /**
     * Some Android Bluetooth stacks silently stop delivering callbacks after a long
     * continuous low-latency scan. Recreate the scan before that device-specific limit.
     */
    private val plannedScanRefresh = Runnable {
        if (isScanning && !preferences.activeSensorId.isNullOrBlank()) {
            scheduleScanRestart("planned_refresh")
        }
    }

    private val connectionWatchdog = object : Runnable {
        override fun run() {
            val now = System.currentTimeMillis()
            val lastSeenAt = preferences.lastAdvertisementAtEpochMillis
                .takeIf { it >= scanStartedAtEpochMillis }
                ?: 0L
            val lastActivityAt = lastSeenAt.takeIf { it > 0 } ?: scanStartedAtEpochMillis
            val packetsTimedOut = lastActivityAt > 0 && now - lastActivityAt >= CONNECTION_LOST_TIMEOUT_MILLIS
            if (lastSeenAt > 0 && packetsTimedOut) {
                if (!preferences.lossNotificationSent) {
                    preferences.lossNotificationSent = true
                    updateForegroundStatus(ConnectionStatus.DISCONNECTED)
                }
            }
            if (packetsTimedOut && now - lastScanRestartAtEpochMillis >= SCAN_RESTART_INTERVAL_MILLIS) {
                scheduleScanRestart("packets_timeout")
            }
            handler.postDelayed(this, WATCHDOG_INTERVAL_MILLIS)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        getSystemService(NotificationManager::class.java).cancel(LEGACY_STATUS_NOTIFICATION_ID)
        startForeground(FOREGROUND_NOTIFICATION_ID, foregroundNotification(ConnectionStatus.WAITING))
        handler.post(connectionWatchdog)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopMonitoring()
            stopSelf()
            return START_NOT_STICKY
        }

        if (preferences.activeSensorId.isNullOrBlank()) {
            Log.w(TAG, "NMG_SERVICE_STOP no active sensor")
            stopSelf()
            return START_NOT_STICKY
        }
        Log.i(TAG, "NMG_SERVICE_START sensorId=${preferences.activeSensorId}")
        startScanning()
        return START_STICKY
    }

    override fun onDestroy() {
        stopMonitoring()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    @SuppressLint("MissingPermission")
    private fun startScanning() {
        if (isScanning) return
        if (!hasScanPermission()) {
            Log.w(TAG, "NMG_SCAN_SKIPPED missing_scan_permission")
            return
        }
        if (!bluetoothAdapter.isEnabled) {
            Log.w(TAG, "NMG_SCAN_SKIPPED bluetooth_disabled")
            return
        }
        val scanner = bluetoothAdapter.bluetoothLeScanner ?: run {
            Log.w(TAG, "NMG_SCAN_SKIPPED scanner_unavailable")
            return
        }
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(0)
            .build()
        runCatching {
            scanner.startScan(emptyList(), settings, scanCallback)
        }.onSuccess {
            isScanning = true
            scanStartedAtEpochMillis = System.currentTimeMillis()
            Log.i(TAG, "NMG_SCAN_STARTED")
            handler.removeCallbacks(plannedScanRefresh)
            handler.postDelayed(plannedScanRefresh, PLANNED_SCAN_REFRESH_MILLIS)
        }.onFailure { error ->
            Log.e(TAG, "NMG_SCAN_START_FAILED", error)
            scheduleScanRestart("start_exception")
        }
    }

    @SuppressLint("MissingPermission")
    private fun scheduleScanRestart(reason: String) {
        if (preferences.activeSensorId.isNullOrBlank()) return
        val now = System.currentTimeMillis()
        lastScanRestartAtEpochMillis = now
        handler.removeCallbacks(scanRestart)
        handler.removeCallbacks(plannedScanRefresh)
        if (isScanning && hasScanPermission()) {
            bluetoothAdapter.bluetoothLeScanner?.stopScan(scanCallback)
        }
        isScanning = false
        Log.w(TAG, "NMG_SCAN_RESTART reason=$reason")
        handler.postDelayed(scanRestart, SCAN_RESTART_DELAY_MILLIS)
    }

    @SuppressLint("MissingPermission")
    private fun stopMonitoring() {
        handler.removeCallbacks(connectionWatchdog)
        handler.removeCallbacks(scanRestart)
        handler.removeCallbacks(plannedScanRefresh)
        if (isScanning && hasScanPermission()) {
            bluetoothAdapter.bluetoothLeScanner?.stopScan(scanCallback)
        }
        isScanning = false
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun handleScanResult(result: ScanResult) {
        val record = result.scanRecord ?: return
        if (!record.deviceName.equals(NMG_DEVICE_NAME, ignoreCase = true)) return

        val advertisement = NmgAdvertisementParser.parse(record) ?: run {
            Log.w(TAG, "NMG_PACKET_REJECTED reason=invalid_advertisement")
            return
        }
        if (!advertisement.sensorId.equals(preferences.activeSensorId, ignoreCase = true)) {
            Log.d(TAG, "NMG_PACKET_REJECTED reason=other_sensor sensorId=${advertisement.sensorId}")
            return
        }

        val receivedAt = System.currentTimeMillis()
        val previousAdvertisementAt = preferences.lastAdvertisementAtEpochMillis
        val lastKnownUptimeSeconds = measurementStore.latestUptimeSeconds(advertisement.sensorId)
        val shouldRecoverHistory = previousAdvertisementAt > 0 &&
            receivedAt - previousAdvertisementAt >= HISTORY_RECOVERY_GAP_MILLIS &&
            lastKnownUptimeSeconds != null &&
            NmgHistoryRecoveryPolicy.hasMissingHistoryBucket(
                lastStoredUptimeSeconds = lastKnownUptimeSeconds,
                currentUptimeSeconds = advertisement.uptimeSeconds
            )

        // Capture the last point before writing the returning live packet. Otherwise the
        // history request would start after the gap and could not fill it.
        gattInspector.inspect(
            device = result.device,
            advertisement = advertisement,
            receivedAtEpochMillis = receivedAt,
            lastKnownUptimeSeconds = lastKnownUptimeSeconds,
            forceHistoryReconciliation = shouldRecoverHistory
        )
        measurementStore.save(advertisement, receivedAt)
        preferences.saveLiveMeasurement(advertisement, receivedAt)
        if (shouldRecoverHistory) {
            Log.i(
                TAG,
                "NMG_HISTORY_RECOVERY_REQUESTED sensorId=${advertisement.sensorId} " +
                    "gapMillis=${receivedAt - previousAdvertisementAt} " +
                    "from=$lastKnownUptimeSeconds to=${advertisement.uptimeSeconds}"
            )
        }
        Log.i(TAG, "NMG_ADVERTISEMENT sensorId=${advertisement.sensorId} uptime=${advertisement.uptimeSeconds}")
        preferences.lastAdvertisementAtEpochMillis = receivedAt
        updateForegroundStatus(ConnectionStatus.CONNECTED)
        if (preferences.lossNotificationSent) {
            preferences.lossNotificationSent = false
            updateForegroundStatus(ConnectionStatus.RESTORED)
        }
    }

    private fun hasScanPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) ==
                PackageManager.PERMISSION_GRANTED

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(
                    MONITORING_CHANNEL_ID,
                    "Мониторинг НМГ",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Статус фонового получения данных НМГ по Bluetooth"
                }
            )
        }
    }

    private fun updateForegroundStatus(status: ConnectionStatus) {
        if (connectionStatus == status) return
        connectionStatus = status
        getSystemService(NotificationManager::class.java).notify(
            FOREGROUND_NOTIFICATION_ID,
            foregroundNotification(status)
        )
    }

    private fun foregroundNotification(status: ConnectionStatus): Notification = NotificationCompat.Builder(this, MONITORING_CHANNEL_ID)
        .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
        .setContentTitle(status.title)
        .setContentText(status.text)
        .setOngoing(true)
        .setOnlyAlertOnce(status != ConnectionStatus.DISCONNECTED)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setCategory(NotificationCompat.CATEGORY_SERVICE)
        .build()

    companion object {
        private const val TAG = "NmgMonitoring"
        private const val ACTION_STOP = "com.elta.android.action.STOP_NMG_MONITORING"
        private const val NMG_DEVICE_NAME = "ELTA"
        private const val MONITORING_CHANNEL_ID = "nmg_monitoring_status_v2"
        private const val FOREGROUND_NOTIFICATION_ID = 6201
        private const val LEGACY_STATUS_NOTIFICATION_ID = 6202
        private const val CONNECTION_LOST_TIMEOUT_MILLIS = 90_000L
        private const val HISTORY_RECOVERY_GAP_MILLIS = 12_000L
        private const val WATCHDOG_INTERVAL_MILLIS = 30_000L
        private const val SCAN_RESTART_INTERVAL_MILLIS = 30_000L
        private const val SCAN_RESTART_DELAY_MILLIS = 2_000L
        private const val PLANNED_SCAN_REFRESH_MILLIS = 4 * 60_000L + 15_000L

        fun newStartIntent(context: Context): Intent = Intent(context, CgmMonitoringService::class.java)

        fun newStopIntent(context: Context): Intent =
            Intent(context, CgmMonitoringService::class.java).setAction(ACTION_STOP)
    }

    private enum class ConnectionStatus(val title: String, val text: String) {
        WAITING("НМГ: ожидание данных", "Ищем сохранённый НМГ датчик по Bluetooth"),
        CONNECTED("НМГ подключён", "Данные датчика поступают в приложение"),
        DISCONNECTED("НМГ: нет связи", "Нет новых данных. Проверьте, что датчик рядом"),
        RESTORED("НМГ подключён", "Связь с датчиком восстановлена")
    }
}
