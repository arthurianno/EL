package com.elta.android.data.features.devices.glucometer.client

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import com.elta.android.common.errors.CommandError
import com.elta.android.common.errors.CommandStillWritingError
import com.elta.android.common.logger.crashlyrics.CrashlyticsReport
import com.elta.android.data.features.devices.dto.VersionDto
import com.elta.android.data.features.devices.glucometer.command.Commands
import com.elta.android.data.features.devices.glucometer.service.isPinOk
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.PhyRequest
import no.nordicsemi.android.ble.ktx.suspend
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime
import java.nio.charset.Charset
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GlucometerBleManager @Inject constructor(
    private val crashlyticsReport: CrashlyticsReport,
    context: Context
) : BleManager(context),
    GlucometerCommand {
    class Builder {
        private var mtu: Int = DEFAULT_MTU_VALUE
        private var connectionPriority: Int? = null
        private var crashlyticsReport: CrashlyticsReport? = null
        private var context: Context? = null

        fun setMtu(value: Int) = apply { mtu = value }
        fun setConnectionPriority(value: Int?) = apply { connectionPriority = value }
        fun setCrashlyticsReport(report: CrashlyticsReport) = apply { crashlyticsReport = report }
        fun setContext(context: Context) = apply { this.context = context }
        fun build(): GlucometerBleManager {
            if (context == null || crashlyticsReport == null)
                throw Exception("Not implemented context or crashlytics")

            return GlucometerBleManager(crashlyticsReport!!, context!!)
                .apply {
                    this.mtuValue = mtu
                    this.connectionPriorityValue = connectionPriority
                }
        }
    }

    private var mtuValue: Int = DEFAULT_MTU_VALUE
    private var connectionPriorityValue: Int? = null

    /**
     * GATT характеристика для передачи комманд в глюкометр
     */
    private var glucometerCharacteristic: BluetoothGattCharacteristic? = null

    /**
     * GATT характеристика для подписок, все результаты выполнения комманд получаем через нее
     */
    private var notificationCharacteristic: BluetoothGattCharacteristic? = null

    @SuppressLint("MissingPermission")
    override suspend fun connectToGlucometer(device: BluetoothDevice) {
        connect(device)
            .retry(10, 600)
            .useAutoConnect(false)
            .usePreferredPhy(PhyRequest.PHY_LE_1M_MASK or PhyRequest.PHY_LE_2M_MASK)
            .timeout(30_000)
            .suspend()
    }

    fun isConnected(deviceAddress: String): Boolean {
        return bluetoothDevice?.address == deviceAddress && isConnected
    }

    override suspend fun disconnectGlucometer() {
        if (isConnected) {
            disconnect().suspend()
        }
    }

    override suspend fun checkPin(pin: String): Boolean {
        val result = startCommand(Commands.SetPin(pin))
        return result.isPinOk()
    }

    override suspend fun toBootMode(): String =
        startCommand(Commands.ToBootMode).checkCommandForError()

    override suspend fun getDate(): ZonedDateTime {
        val dateTime = startCommand(Commands.GetDate)
        return ZonedDateTime.of(dateTime.extractDate(), ZoneOffset.UTC)
    }

    override suspend fun getZoneOffsetSeconds(): Int =
        startCommand(Commands.GetZone).extractZoneOffsetSeconds()

    override suspend fun getVersion(): VersionDto =
        startCommand(Commands.GetVersion).extractVersion()

    override suspend fun getBatteryAndTemperature(): Pair<Int, Int> =
        startCommand(Commands.GetBatteryAndTemperature).extractBatteryAndTemperature()

    override suspend fun turnOnFindMode(): String =
        startCommand(Commands.TurnOnFindMode).checkCommandForError()

    override suspend fun updateTime(date: ZonedDateTime): String =
        startCommand(Commands.SetTime(date)).checkCommandForError()

    override suspend fun updateZoneOffset(offsetSeconds: Int): String =
        startCommand(Commands.SetZone(offsetSeconds.toZoneHexString())).checkCommandForError()

    override suspend fun getSerialNumber(): String =
        startCommand(Commands.Serial).extractSerial()

    override suspend fun getError(): Long =
        startCommand(Commands.GetError).extractErrorWord()


    override suspend fun readEvent(index: Int): String =
        startCommand(Commands.ReadEvent(index))

    override suspend fun readMemoryEvent(index: Int): String =
        startCommand(Commands.ReadMemoryEvent(index))

    override suspend fun sendFirmwareChunk(chunk: FirmwareChunk): String =
        sendFirmwareChunkCommand(chunk).checkChunkWritingResult(chunk)

    fun getConnectedDeviceName(): String? = bluetoothDevice?.name

    private suspend fun startCommand(command: Commands): String {
        require(command !is Commands.SendFirmwareChunk) {
            "SendFirmwareChunk must be handled by sendFirmwareChunkCommand()"
        }
        val response = writeAndAwaitNotification(
            payload = command.getByteCommand(),
            requestLog = when (command) {
                is Commands.SetPin -> "Sent pin to device"
                else -> "Sent $command"
            }
        )
        val resultResponse = response.toString(Charset.defaultCharset())
        val logResponse = if (command is Commands.Serial) {
            "Serial number received"
        } else {
            resultResponse
        }
        crashlyticsReport.log("Received notification for ${command.javaClass.name} with result: $logResponse")
        return resultResponse
    }

    private suspend fun sendFirmwareChunkCommand(chunk: FirmwareChunk): BootChunkResponse {
        val response = writeAndAwaitNotification(
            payload = chunk.toByteArray(),
            requestLog = "Sent a chunk of bytes"
        )
        val parsedResponse = response.toBootChunkResponse()
        crashlyticsReport.log(
            "Received firmware response: flag=${parsedResponse.flag}, cmd=${parsedResponse.cmd}, " +
                    "adr=${parsedResponse.adr}, num=${parsedResponse.num}"
        )
        return parsedResponse
    }

    private suspend fun writeAndAwaitNotification(
        payload: ByteArray,
        requestLog: String
    ): ByteArray {
        try {
            writeCharacteristic(
                glucometerCharacteristic,
                payload,
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            ).split().suspend()
        } catch (e: Exception) {
            crashlyticsReport.writeException(e)
            throw CommandError(e.message.orEmpty())
        }
        crashlyticsReport.log(requestLog)
        return try {
            waitForNotification(notificationCharacteristic).suspend().value
                ?: throw CommandError("Empty waitForNotification result")
        } catch (e: Exception) {
            crashlyticsReport.writeException(e)
            throw CommandError(e.message.orEmpty())
        }
    }

    /**
     * Метод в котором происходит инициализации сервиса и характеристик
     */
    override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
        gatt.getService(SERVICE_UUID)?.apply {
            glucometerCharacteristic = getCharacteristic(CHAR_UUID)
            notificationCharacteristic = getCharacteristic(NOTIFICATION_UUID)
            return glucometerCharacteristic != null && notificationCharacteristic != null
        }
        return false
    }

    override fun initialize() {
        enableNotifications(notificationCharacteristic)
            .enqueue()
        connectionPriorityValue?.let { requestConnectionPriority(it) }
        requestMtu(mtuValue).enqueue()
    }

    override fun onServicesInvalidated() {
        disableNotifications(notificationCharacteristic)
        glucometerCharacteristic = null
        notificationCharacteristic = null
    }

    @Throws(CommandError::class)
    private fun String.checkCommandForError(): String {
        return if (isError()) throw CommandError(this) else this
    }

    private fun String.isError(): Boolean = contains("error")

    private fun BootChunkResponse.checkChunkWritingResult(chunk: FirmwareChunk): String {
        if (cmd != chunk.cmd || adr != chunk.adr || num != chunk.num) {
            throw CommandError(
                "Boot response mismatch. Expected cmd=${chunk.cmd}, adr=${chunk.adr}, num=${chunk.num}; " +
                        "received cmd=$cmd, adr=$adr, num=$num"
            )
        }
        return when (flag) {
            0x00 -> flag.toString()
            0x01 -> throw CommandStillWritingError
            0x02 -> throw CommandError("Command ended with error")
            else -> throw CommandError("Command not accepted. Format or syntax error")
        }
    }

    private fun ByteArray.toBootChunkResponse(): BootChunkResponse {
        if (size < BOOT_MODE_RESPONSE_SIZE_BYTES) {
            throw CommandError("Invalid boot response size: $size")
        }
        val responseCmd = this[1]
        val responseAddress =
            (this[2].toInt() and BYTE_MASK) or
                    ((this[3].toInt() and BYTE_MASK) shl 8) or
                    ((this[4].toInt() and BYTE_MASK) shl 16) or
                    ((this[5].toInt() and BYTE_MASK) shl 24)
        val responseNum = (this[6].toInt() and BYTE_MASK).toUByte()
        return BootChunkResponse(
            flag = this[0].toInt() and BYTE_MASK,
            cmd = responseCmd,
            adr = responseAddress,
            num = responseNum
        )
    }
}

private data class BootChunkResponse(
    val flag: Int,
    val cmd: Byte,
    val adr: Int,
    val num: UByte
)

private val SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")
private val CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e")
private val NOTIFICATION_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e")

private const val DEFAULT_MTU_VALUE = 512
private const val BOOT_MODE_RESPONSE_SIZE_BYTES = 7
private const val BYTE_MASK = 0xFF
