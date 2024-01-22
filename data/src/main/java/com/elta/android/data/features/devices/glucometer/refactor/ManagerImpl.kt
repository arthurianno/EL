package com.elta.android.data.features.devices.glucometer.refactor

import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import com.elta.android.common.errors.GlucometerPinIncorrectOrNotFoundError
import com.elta.android.data.features.devices.dto.GlucometerEventDto
import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import com.elta.android.data.features.devices.glucometer.builder.GlucometerEventBuilder
import com.elta.android.data.features.devices.glucometer.service.isEmptyEvent
import com.elta.android.data.features.devices.glucometer.service.isEvent
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

@Singleton
class ManagerImpl @Inject constructor(
    private val scannerService: ScannerService,
    private val glucometerBleManager: GlucometerBleManager,

    private val eventBuilder: GlucometerEventBuilder //TODO: точно ли тут
) : Manager {

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

    override fun findDevices(): Flow<List<ScanResult>> {
        Timber.tag(TAG).d("Start find devices")
        return callbackFlow {
            scannerService.startScan(filters, settings) {
                Timber.tag(TAG).d("ScanResult :: $it")
                trySend(it) //TODO: в сценарии первого подключения, когда пользователь выбирает из разных - необходимо остановить поиск после выбора.
            }
            awaitClose {
                scannerService.stopScan()
            }
        }
            .runningFold(
                emptyList(),
                operation = { accumulator: List<ScanResult>, new: List<ScanResult> ->
                    (accumulator + new).distinctBy { it.device.address }
                })
    }

    override suspend fun getGlucometerInfo(address: String, pin: String): GlucometerInfoDto {
        Timber.tag(TAG).d("Start get glucometer info")
        val scanResult = scan(address)
        with(glucometerBleManager) {
            connectToGlucometer(scanResult.device)
            checkPin(pin)
            val date = getDate()
            val (battery, temperature) = getBatteryAndTemperature()
            val version = getVersion()
            val serial = getSerialNumber()
            val glucometerInfo = GlucometerInfoDto(
                id = address,
                deviceDate = date,
                syncDate = ZonedDateTime.now(),
                temperature = temperature,
                batteryLevel = battery,
                version = version,
                glucometerSerialNumber = serial,
                lastSyncedEvent = null //TODO: убрать бы отсюда это и поместить в use case, т.к. тут неи тнформции о последнем синке
            )

            disconnectGlucometer()

            return glucometerInfo
        }
    }

    override suspend fun connectDevice(address: String, pin: String) {
        Timber.tag(TAG).d("Start connect to glucometer")
        val scanResult = scan(address)
        with(glucometerBleManager) {
            connectToGlucometer(scanResult.device)
            val pinIsValid = checkPin(pin)
            if (!pinIsValid) {
                disconnectGlucometer()
                throw GlucometerPinIncorrectOrNotFoundError
            }
        }
    }

    override suspend fun syncWithDevice(address: String, pin: String, email: String): List<GlucometerEventDto> {
        Timber.tag(TAG).d("Start sync with glucometer")
        val scanResult = scan(address)
        with(glucometerBleManager) {
            connectToGlucometer(scanResult.device)
            checkPin(pin)
            //TODO: при синхронизации в старом коде идет и обновление данных,
            // и само вытягивание значений, надо это поделить
            val serial = getSerialNumber()
            val events = mutableListOf<GlucometerEventDto>()

            //TODO: рефактор
            run repeatBlock@ {
                repeat(1000) { index ->
                    val event = readEvent(index)

                    //TODO: сюда прописать условия остановки
                    // Пустое событие isEmptyEvent ИЛИ last Event

                    if (event.isEmptyEvent()) return@repeatBlock
                    val eventDto = eventBuilder.buildFrom(
                        email,
                        address,
                        event,
                        serial
                    )
                    events.add(eventDto)
                }
            }

            disconnectGlucometer()
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
            scannerService.startScan(filters = filters, settings = settings) { scanResults ->
                scanResults.firstOrNull { it.device.address == address }?.let { result ->
                    try {
                        Timber.tag(TAG).d("Device with address ${result.device.address} found")
                        continuation.resume(result)
                    } catch (ex: IllegalStateException) {
                        Timber.tag(TAG).d("Error: Device with address ${result.device.address} not found")
                        scannerService.stopScan()
                    } finally {
                        scannerService.stopScan()
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