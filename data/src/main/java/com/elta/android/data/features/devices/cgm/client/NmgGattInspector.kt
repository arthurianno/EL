package com.elta.android.data.features.devices.cgm.client

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.elta.android.data.features.devices.cgm.protocol.NmgAdvertisement
import com.elta.android.data.features.devices.cgm.protocol.NmgHistoryPacketParser
import com.elta.android.data.features.devices.cgm.storage.NmgMeasurementStore
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** Reads the sensor's stored points through its F0001110 / F0001111 GATT path. */
@Singleton
class NmgGattInspector @Inject constructor(
    private val context: Context,
    private val measurementStore: NmgMeasurementStore
) {
    private val handler = Handler(Looper.getMainLooper())

    private var inspectionInProgress = false
    private var nextHistoryReconciliationAtMillis = 0L
    private var historyCharacteristic: BluetoothGattCharacteristic? = null
    private var session: HistorySession? = null
    private var writeInProgress = false
    private var readInProgress = false
    private var pendingRecoveryStartUptimeSeconds: Long? = null

    @SuppressLint("MissingPermission")
    fun inspect(
        device: BluetoothDevice,
        advertisement: NmgAdvertisement,
        receivedAtEpochMillis: Long,
        lastKnownUptimeSeconds: Long? = null,
        forceHistoryReconciliation: Boolean = false
    ) {
        if (forceHistoryReconciliation) {
            pendingRecoveryStartUptimeSeconds = lastKnownUptimeSeconds
        }
        val recoveryStartUptimeSeconds = pendingRecoveryStartUptimeSeconds
        if (
            inspectionInProgress ||
            (recoveryStartUptimeSeconds == null &&
                receivedAtEpochMillis < nextHistoryReconciliationAtMillis)
        ) return

        // A smaller counter means that the sensor has started a new session. Its old history
        // must not be joined to this session solely by matching the MAC address.
        val historyStartUptimeSeconds = recoveryStartUptimeSeconds ?: lastKnownUptimeSeconds
        if (historyStartUptimeSeconds != null && historyStartUptimeSeconds >= advertisement.uptimeSeconds) {
            pendingRecoveryStartUptimeSeconds = null
            Log.i(
                TAG,
                "NMG_HISTORY_SKIPPED sensorId=${advertisement.sensorId} " +
                    "reason=sensor_uptime_reset previous=$historyStartUptimeSeconds " +
                    "current=${advertisement.uptimeSeconds}"
            )
            return
        }

        inspectionInProgress = true
        val lastStoredTimestamp = historyStartUptimeSeconds
            ?: measurementStore.latestUptimeSeconds(advertisement.sensorId)
        if (recoveryStartUptimeSeconds != null) {
            Log.i(
                TAG,
                "NMG_HISTORY_RECOVERY sensorId=${advertisement.sensorId} " +
                    "from=$lastStoredTimestamp to=${advertisement.uptimeSeconds}"
            )
        }
        session = HistorySession(
            sensorId = advertisement.sensorId,
            state = advertisement.state,
            latestTimestampSeconds = advertisement.uptimeSeconds,
            receivedAtEpochMillis = receivedAtEpochMillis,
            isRecovery = recoveryStartUptimeSeconds != null,
            nextTimestampSeconds = lastStoredTimestamp?.plus(HISTORY_POINT_INTERVAL_SECONDS)
                ?: (advertisement.uptimeSeconds - HISTORY_RETENTION_SECONDS).coerceAtLeast(0)
        )
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                device.connectGatt(context, false, callback, BluetoothDevice.TRANSPORT_LE)
            } else {
                device.connectGatt(context, false, callback)
            }
        }.onFailure { error ->
            finish(null, completed = false)
            Log.w(TAG, "Unable to connect to NMG sensor", error)
        }
    }

    private val callback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothGatt.STATE_CONNECTED) {
                gatt.discoverServices()
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED || status != BluetoothGatt.GATT_SUCCESS) {
                if (inspectionInProgress) {
                    finish(gatt, completed = false)
                } else {
                    gatt.close()
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG, "NMG service discovery failed: status=$status")
                finish(gatt, completed = false)
                return
            }

            val characteristic = gatt.getService(CONTROL_SERVICE_UUID)
                ?.getCharacteristic(HISTORY_CHARACTERISTIC_UUID)
            if (characteristic == null ||
                characteristic.properties and BluetoothGattCharacteristic.PROPERTY_READ == 0 ||
                characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE == 0
            ) {
                Log.w(TAG, "NMG history characteristic is unavailable")
                finish(gatt, completed = false)
                return
            }

            historyCharacteristic = characteristic
            requestHistory(gatt)
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            handleHistoryRead(gatt, characteristic, characteristic.value, status)
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            handleHistoryRead(gatt, characteristic, value, status)
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            handleHistoryWrite(gatt, characteristic, status)
        }

    }

    @SuppressLint("MissingPermission", "DEPRECATION")
    private fun requestHistory(gatt: BluetoothGatt) {
        val currentSession = session ?: run {
            finish(gatt, completed = false)
            return
        }
        val characteristic = historyCharacteristic ?: run {
            finish(gatt, completed = false)
            return
        }

        characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        characteristic.value = historyCommand(currentSession.nextTimestampSeconds)
        writeInProgress = true
        if (!gatt.writeCharacteristic(characteristic)) {
            writeInProgress = false
            Log.w(TAG, "NMG history command was rejected")
            finish(gatt, completed = false)
        }
    }

    @SuppressLint("MissingPermission")
    private fun handleHistoryWrite(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        if (!writeInProgress || characteristic.uuid != HISTORY_CHARACTERISTIC_UUID) return
        writeInProgress = false
        if (status != BluetoothGatt.GATT_SUCCESS) {
            Log.w(TAG, "NMG history command failed: status=$status")
            finish(gatt, completed = false)
            return
        }
        handler.postDelayed({
            if (session != null) {
                readInProgress = true
                if (!gatt.readCharacteristic(characteristic)) {
                    Log.w(TAG, "NMG history response read was rejected")
                    finish(gatt, completed = false)
                }
            }
        }, HISTORY_RESPONSE_DELAY_MILLIS)
    }

    private fun handleHistoryRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        status: Int
    ) {
        if (!readInProgress || characteristic.uuid != HISTORY_CHARACTERISTIC_UUID) return
        readInProgress = false

        val currentSession = session ?: run {
            finish(gatt, completed = false)
            return
        }
        val points = if (status == BluetoothGatt.GATT_SUCCESS) {
            NmgHistoryPacketParser.parse(value)
        } else {
            null
        }
        if (points.isNullOrEmpty()) {
            Log.w(TAG, "NMG history response is invalid: status=$status, size=${value.size}")
            finish(gatt, completed = false)
            return
        }

        measurementStore.saveHistory(
            sensorId = currentSession.sensorId,
            state = currentSession.state,
            points = points,
            latestTimestampSeconds = currentSession.latestTimestampSeconds,
            receivedAtEpochMillis = currentSession.receivedAtEpochMillis
        )

        val responseStartSeconds = points.first().uptimeSeconds
        val responseEndSeconds = points.last().uptimeSeconds
        val reachedLatestPoint = responseEndSeconds >= currentSession.latestTimestampSeconds
        val repeatedResponse = currentSession.previousResponseStartSeconds == responseStartSeconds
        val reachedRequestLimit = currentSession.requestCount >= MAX_HISTORY_REQUESTS
        if (reachedLatestPoint || repeatedResponse || reachedRequestLimit) {
            Log.i(TAG, "NMG history read complete: ${currentSession.requestCount + 1} packet(s)")
            finish(gatt, completed = true)
            return
        }

        session = currentSession.copy(
            nextTimestampSeconds = responseEndSeconds + HISTORY_POINT_INTERVAL_SECONDS,
            previousResponseStartSeconds = responseStartSeconds,
            requestCount = currentSession.requestCount + 1
        )
        handler.postDelayed({ requestHistory(gatt) }, NEXT_HISTORY_REQUEST_DELAY_MILLIS)
    }

    private fun historyCommand(timestampSeconds: Long): ByteArray = ByteBuffer.allocate(HISTORY_COMMAND_SIZE_BYTES)
        .order(ByteOrder.LITTLE_ENDIAN)
        .put(HISTORY_COMMAND)
        .putInt(timestampSeconds.toInt())
        .array()

    @SuppressLint("MissingPermission")
    private fun finish(gatt: BluetoothGatt?, completed: Boolean) {
        val completedRecovery = session?.isRecovery == true
        handler.removeCallbacksAndMessages(null)
        inspectionInProgress = false
        nextHistoryReconciliationAtMillis = System.currentTimeMillis() +
            if (completed) HISTORY_RECONCILIATION_INTERVAL_MILLIS else HISTORY_RETRY_DELAY_MILLIS
        if (completed && completedRecovery) {
            pendingRecoveryStartUptimeSeconds = null
        }
        writeInProgress = false
        readInProgress = false
        historyCharacteristic = null
        session = null
        gatt?.disconnect()
        gatt?.close()
    }

    private data class HistorySession(
        val sensorId: String,
        val state: Int,
        val latestTimestampSeconds: Long,
        val receivedAtEpochMillis: Long,
        val isRecovery: Boolean,
        val nextTimestampSeconds: Long,
        val previousResponseStartSeconds: Long? = null,
        val requestCount: Int = 0
    )

    private companion object {
        const val TAG = "NmgGattInspector"
        const val HISTORY_RETENTION_SECONDS = 24 * 60 * 60L
        const val HISTORY_POINT_INTERVAL_SECONDS = 120L
        const val HISTORY_RECONCILIATION_INTERVAL_MILLIS = 15 * 60 * 1_000L
        const val HISTORY_RETRY_DELAY_MILLIS = 30_000L
        const val MAX_HISTORY_REQUESTS = 144
        const val HISTORY_RESPONSE_DELAY_MILLIS = 150L
        const val NEXT_HISTORY_REQUEST_DELAY_MILLIS = 100L
        const val HISTORY_COMMAND: Byte = 0x01
        const val HISTORY_COMMAND_SIZE_BYTES = 5

        val CONTROL_SERVICE_UUID: UUID = UUID.fromString("F0001110-0451-4000-B000-000000000000")
        val HISTORY_CHARACTERISTIC_UUID: UUID = UUID.fromString("F0001111-0451-4000-B000-000000000000")
    }
}
