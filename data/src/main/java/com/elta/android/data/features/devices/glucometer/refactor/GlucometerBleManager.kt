package com.elta.android.data.features.devices.glucometer.refactor

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import com.elta.android.data.features.devices.dto.VersionDto
import com.elta.android.data.features.devices.glucometer.command.Commands
import com.elta.android.data.features.devices.glucometer.service.isPinOk
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.PhyRequest
import no.nordicsemi.android.ble.ktx.suspend
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import timber.log.Timber
import java.nio.charset.Charset
import java.util.UUID
import javax.inject.Inject


class GlucometerBleManager @Inject constructor(
    context: Context,

    ) : BleManager(context),
    GlucometerCommand {

    private var glucometerCharacteristic: BluetoothGattCharacteristic? = null
    private var notificationCharacteristic: BluetoothGattCharacteristic? = null

    @SuppressLint("MissingPermission")
    override suspend fun connectToGlucometer(device: BluetoothDevice) {
        connect(device)
            .retry(3, 100)
            .useAutoConnect(false)
//            .usePreferredPhy(PhyRequest.PHY_LE_1M_MASK or PhyRequest.PHY_LE_2M_MASK or PhyRequest.PHY_LE_CODED_MASK)
            .timeout(30_000)
            .suspend()
    }

    override suspend fun disconnectGlucometer() {
        disconnect().suspend()
        close()
    }

    override suspend fun checkPin(pin: String): Boolean {
        val result = startCommand(Commands.SetPin(pin))
        return result.isPinOk()
    }

    override suspend fun toDfuMode(): String {
        return startCommand(Commands.ToDfuMode)
    }

    override suspend fun getDate(): ZonedDateTime {
        val dateTime = startCommand(Commands.GetDate)
        return ZonedDateTime.of(dateTime.extractDate(), ZoneId.systemDefault())
    }

    override suspend fun getVersion(): VersionDto {
        val version = startCommand(Commands.GetVersion)
        return version.extractVersion()
    }

    override suspend fun getBatteryAndTemperature(): Pair<Int, Int> {
        val batteryAndTemperature = startCommand(Commands.GetBatteryAndTemperature)
        return batteryAndTemperature.extractBatteryAndTemperature()
    }

    override suspend fun turnOnFindMode(): String {
        return startCommand(Commands.TurnOnFindMode)
    }

    override suspend fun updateTime(date: ZonedDateTime): String {
        return startCommand(Commands.SetTime(date))
    }

    override suspend fun getSerialNumber(): String {
        val serial = startCommand(Commands.Serial)
        return serial.extractSerial()
    }

    override suspend fun readEvent(index: Int): String {
        return startCommand(Commands.ReadEvent(index))
    }

    private suspend fun startCommand(command: Commands): String {

        val byteCommand = command.getByteCommand()

        val request = writeCharacteristic(
            glucometerCharacteristic,
            byteCommand,
            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        )
            .split()
            .suspend()

        val requestResult = request.value?.toString(Charset.defaultCharset()) ?: throw Exception("Empty writeCharacteristic result")
        Timber.tag(TAG).d("sent $command with result: $requestResult")

        val response = waitForNotification(notificationCharacteristic).suspend()

        val resultResponse = response.value?.toString(Charset.defaultCharset()) ?: throw Exception("Empty waitForNotification result")
        Timber.tag(TAG).d("received notification for $command with result: $resultResponse")

        return resultResponse
    }


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
    }

    override fun onServicesInvalidated() {
        glucometerCharacteristic = null
        notificationCharacteristic = null
    }
}

private val SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")
private val CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e")
private val NOTIFICATION_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e")

private const val TAG = "GLUCOMETER_BLE_MANAGER"

