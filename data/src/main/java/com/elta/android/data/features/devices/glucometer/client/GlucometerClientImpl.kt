package com.elta.android.data.features.devices.glucometer.client

import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import com.elta.android.common.errors.GlucometerNotConnectedException
import com.elta.android.common.errors.GlucometerPinIncorrect
import com.elta.android.data.features.devices.dto.GlucometerEventDto
import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import com.elta.android.data.features.devices.glucometer.builder.GlucometerEventBuilder
import com.elta.android.data.features.devices.glucometer.service.isEmptyEvent
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.runningFold
import org.threeten.bp.ZonedDateTime
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.jvm.Throws

@Singleton
class GlucometerClientImpl @Inject constructor(
    private val environmentScanner: EnvironmentScanner,
    private val glucometerBleManager: GlucometerBleManager,
    private val eventBuilder: GlucometerEventBuilder //TODO: точно ли тут
) : GlucometerClient {

    //TODO: для каждой команды надо сделать:
    // 1. проверку что уже не подсоеденено устройство
    // 2. scan()
    // 3. connect()
    // 4. любые команды
    // 5. disconnect()
    // Чтобы минимизировать риск неправильной очередности команд или "забыл сканирование"

    //TODO: стоит проверять на connect перед операцией и только тогда его производить.
    // Проверять, не привязанно ли уже устройство,
    // 1. если привязано, то выполнять команды
    // 1.1. пройден пин или нет
    // 2. если не привязано, то привязать, проверить пин

    // TODO: Доступы к бд и иной логике в UseCase!

    // TODO: для таймера timeout в UseCase


    private val settings: ScanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
        .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
        .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
        .setReportDelay(0)
        .build()

    private val filters: List<ScanFilter> = listOf<ScanFilter>(
        ScanFilter.Builder()
            .setDeviceName("Satellite")
            .build()
    )

        // todo example of dfu scanning
    private val filterDfu: List<ScanFilter> = listOf<ScanFilter>(
        ScanFilter.Builder()
            .setDeviceName("Dfu")
            .build()
    )
    // todo fix

    suspend fun update(address: String, pin: String){
        val scan = scan(address)
        glucometerBleManager.connectToGlucometer(scan.device)
        glucometerBleManager.checkPin(pin)
        glucometerBleManager.toDfuMode()
        glucometerBleManager.disconnectGlucometer()

//        val scanDfu = scan(address, filterDfu)
//        glucometerBleManager.connectToGlucometer(scanDfu.device)
       // TODO() доделать glucometerBleManager.dfuUpdate()

    }

    override fun findDevices(): Flow<List<ScanResult>> {
        Timber.tag(TAG).d("Start find devices")
        return callbackFlow {
            environmentScanner.startScan(filters, settings) {
                Timber.tag(TAG).d("ScanResult :: $it")
                trySend(it) //TODO: в сценарии первого подключения, когда пользователь выбирает из разных
            // - необходимо остановить поиск после выбора нужного пользователю устройства.
            }
            awaitClose { environmentScanner.stopScan() }
        }
            .runningFold(
                emptyList(),
                operation = { accumulator: List<ScanResult>, new: List<ScanResult> ->
                    (accumulator + new).distinctBy { it.device.address }
                })
    }

    @Throws(GlucometerNotConnectedException::class)
    override suspend fun getGlucometerInfo(address: String): GlucometerInfoDto {
        Timber.tag(TAG).d("Start get glucometer info")
        with(glucometerBleManager) {
            if (!glucometerBleManager.isConnected(address)) {
                //TODO: в лог
                throw GlucometerNotConnectedException(address)
            }

            updateTime(ZonedDateTime.now())
            val date = getDate()
            val (battery, temperature) = getBatteryAndTemperature()
            val version = getVersion()
            val serial = getSerialNumber()

            return GlucometerInfoDto(
                id = address,
                deviceDate = date,
                syncDate = ZonedDateTime.now(),
                temperature = temperature,
                batteryLevel = battery,
                version = version,
                glucometerSerialNumber = serial,
                lastSyncedEvent = null //TODO: убрать бы отсюда это и поместить в use case, т.к. тут неи тнформции о последнем синке
            )
        }
    }

    override suspend fun connectDevice(address: String, pin: String) {
        Timber.tag(TAG).d("Start connect to glucometer")
        //TODO: Перед коннектом к устройству нужно просканировать окружение на его наличие, подумать над
        //какой-то более удобной реализацией.

        val scanResult = scan(address)
        glucometerBleManager.connectToGlucometer(scanResult.device)
        val pinIsValid = glucometerBleManager.checkPin(pin)
        if (!pinIsValid) {
            glucometerBleManager.disconnectGlucometer()
            throw GlucometerPinIncorrect
        }
    }

    override suspend fun disconnect() {
        glucometerBleManager.disconnectGlucometer()
    }

    @Throws(GlucometerNotConnectedException::class)
    override suspend fun syncWithDevice(address: String, email: String, serial: String?, lastSyncEvent: String?): List<GlucometerEventDto> {
        Timber.tag(TAG).d("Start sync with glucometer")

        with(glucometerBleManager) {
            if (!isConnected(address)) {
                throw GlucometerNotConnectedException(address)
            }

            val events = mutableListOf<GlucometerEventDto>()

            for (index in 0 until 1000) {
                val event = readEvent(index)
                if (event.isEmptyEvent() || event == lastSyncEvent) break
                events.add(
                    eventBuilder.buildFrom(email, address, event, serial)
                )
            }

            return events
        }
    }

    override suspend fun findGlucometer(address: String, pin: String) {
        Timber.tag(TAG).d("Start find glucometer")
        val scanResult = scan(address)
        with(glucometerBleManager) {
            connectToGlucometer(scanResult.device)
            checkPin(pin)
            repeat(10) { //TODO: сколько раз? Пока не пользователь не остановит?
                turnOnFindMode()
                delay(8000L)
            }
            disconnectGlucometer()
        }
    }

    private suspend fun scan(address: String): ScanResult {
        return suspendCoroutine { continuation ->
            environmentScanner.startScan(filters = filters, settings = settings) { scanResults ->
                scanResults.firstOrNull { it.device.address == address }?.let { result ->
                    try {
                        Timber.tag(TAG).d("Device with address ${result.device.address} found")
                        continuation.resume(result)
                    } catch (ex: IllegalStateException) {
                        Timber.tag(TAG).d("Error: Device with address ${result.device.address} not found")
                        environmentScanner.stopScan()
                    } finally {
                        environmentScanner.stopScan()
                    }
                }
            }
        }
    }

    override suspend fun testAllCommands(address: String, pin: String) {
        Timber.tag(TAG).d("Start all commands")
        val scanResult = scan(address)
        Timber.tag(TAG).d("ScanResult: $scanResult")
        glucometerBleManager.connectToGlucometer(scanResult.device)
        glucometerBleManager.checkPin(pin)
        glucometerBleManager.getDate()
        glucometerBleManager.getVersion()
        glucometerBleManager.getBatteryAndTemperature()
        glucometerBleManager.turnOnFindMode()
        glucometerBleManager.updateTime(ZonedDateTime.now())
        glucometerBleManager.getSerialNumber()
        repeat(1000) { index ->
            glucometerBleManager.readEvent(index)
        }
        glucometerBleManager.toDfuMode()
        glucometerBleManager.disconnectGlucometer()
    }

}

private const val TAG = "BLE_MANAGER"