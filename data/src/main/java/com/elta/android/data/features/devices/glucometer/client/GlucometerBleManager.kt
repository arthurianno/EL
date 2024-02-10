package com.elta.android.data.features.devices.glucometer.client

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import com.elta.android.common.errors.CommandError
import com.elta.android.common.logger.crashlyrics.CrashlyticsReport
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
import kotlin.jvm.Throws

class GlucometerBleManager @Inject constructor(
    private val crashlyticsReport: CrashlyticsReport,
    context: Context
) : BleManager(context),
    GlucometerCommand {

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
        // через bondState можем проверять связаны ли устройства или нет и таким образом отправлять команды далее
        //  device.bondState
        connect(device)
            .retry(3, 100)
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

    override suspend fun toDfuMode(): String =
        startCommand(Commands.ToDfuMode).checkCommandForError()

    override suspend fun getDate(): ZonedDateTime {
        val dateTime = startCommand(Commands.GetDate)
        return ZonedDateTime.of(dateTime.extractDate(), ZoneId.systemDefault())
    }

    override suspend fun getVersion(): VersionDto =
        startCommand(Commands.GetVersion).extractVersion()

    override suspend fun getBatteryAndTemperature(): Pair<Int, Int> =
        startCommand(Commands.GetBatteryAndTemperature).extractBatteryAndTemperature()

    override suspend fun turnOnFindMode(): String =
        startCommand(Commands.TurnOnFindMode).checkCommandForError()

    override suspend fun updateTime(date: ZonedDateTime): String =
        startCommand(Commands.SetTime(date)).checkCommandForError()

    override suspend fun getSerialNumber(): String =
        startCommand(Commands.Serial).extractSerial()

    override suspend fun readEvent(index: Int): String =
        startCommand(Commands.ReadEvent(index))

    private suspend fun startCommand(command: Commands): String {

        //Получение байтового массива из комманды (строки)
        val byteCommand = command.getByteCommand()

        //Запись характеристики методом BleManager. split() устанавливает Mtu(Maximum Transmission Unit)
        //сплиттер и вызывает функцию расширение suspend() для преобразования асинхронного кода
        //c помощью расширения suspend приостанавливается корутина до получения отклика на комманду
        val request = try {
            writeCharacteristic(
                glucometerCharacteristic,
                byteCommand,
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            )
                .split()
                .suspend()
        } catch (e: Exception) {
            crashlyticsReport.writeException(e)
            throw e
        }

        //Результат по сути является индикатором что
        //операция завершена успешно, но сами данные получаем через notificationCharacteristic
        val requestResult = request.value?.toString(Charset.defaultCharset())
            ?: throw Exception("Empty writeCharacteristic result")

        val log = if (command is Commands.SetPin) "Sent pin to device" else "Sent $command with result: $requestResult"
        crashlyticsReport.log(log)


        //Тут подписываемся на уведомление из характеристики notificationCharacteristic,
        //Метод waitForNotification вешает одноразовый коллбек который преобразуется
        //c помощью расширения suspend приостанавливается корутина до получения уведомления
        //suspend тут так же выбрасывает исключения
        val response = try {
            waitForNotification(notificationCharacteristic).suspend()
        } catch (e: Exception) {
            crashlyticsReport.writeException(e)
            throw e
        }

        //Собственно тут получаем реальные замеры устройства в строковом представлении
        val resultResponse = response.value?.toString(Charset.defaultCharset())
            ?: throw Exception("Empty waitForNotification result")

        val logResponse = if (command is Commands.Serial) {
            "Serial number received"
        } else {
            resultResponse
        }
        crashlyticsReport.log("Received notification for ${command.javaClass.name} with result: $logResponse")

        return resultResponse
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
        requestMtu(512).enqueue()
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
}

private val SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")
private val CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e")
private val NOTIFICATION_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e")

private const val TAG = "GLUCOMETER_BLE_MANAGER"

