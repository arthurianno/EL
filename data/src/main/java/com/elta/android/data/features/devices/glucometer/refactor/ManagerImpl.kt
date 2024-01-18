package com.elta.android.data.features.devices.glucometer.refactor

import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import com.elta.android.common.errors.GlucometerPinIncorrectOrNotFoundError
import com.elta.android.data.features.devices.dto.GlucometerEventDto
import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import com.elta.android.data.features.devices.glucometer.builder.GlucometerEventBuilder
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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
        return callbackFlow {
            //FIXME: есть задача, что НУЖНО выводить глюкометры, которые уже подсоеденены, а при попытке подключения выдавать ошибку, что уже привязан
//            val connectedDevices = glucometersCache.getAll(CommonConditions.All)
            scannerService.startScan(filters, settings) {
                trySend(it)
            }
            awaitClose {
                scannerService.stopScan()
            }
        }
    }

    override suspend fun getGlucometerInfo(address: String, pin: String): GlucometerInfoDto {
        val scanResult = scan(address)
        with(glucometerBleManager) {
            connectToGlucometer(scanResult.device)
            checkPin(pin)
            val info = getGlucometerInfo()
            disconnectGlucometer()
            return info
        }
    }

    override suspend fun connectDevice(address: String, pin: String) {
        val scanResult = scan(address)
        with(glucometerBleManager) {
            connectToGlucometer(scanResult.device)
            val pinIsValid = checkPin(pin)
            if (!pinIsValid) {
                disconnectGlucometer()
                throw GlucometerPinIncorrectOrNotFoundError
            }

            //FIXME: по-хорошему вынести на другой уровень
            //pinStorage.setPin(device.address, pinCode)
            //val primaryDevice = glucometersCache.get(GlucometersConditions.Primary)
            //val newDevice = glucometerToCacheMapper.mapFromObject(device)
            //if (primaryDevice == null) {
            //  glucometersCache.add(listOf(newDevice.apply { isPrimary = true }))
            //}
            //
            //if (primaryDevice != null && !primaryDevice.address.equals(
            //                                device.address,
            //                                true)
            //) {
            //glucometersCache.add(listOf(newDevice))
            //}
        }
    }

    override suspend fun syncWithDevice(address: String, pin: String): List<GlucometerEventDto> {
        val scanResult = scan(address)
        with(glucometerBleManager) {
            connectToGlucometer(scanResult.device)
            checkPin(pin)
            //TODO: при синхронизации в старом коде идет и обновление данных,
            // и само вытягивание значений, надо это поделить
            val glucometerInfo = getGlucometerInfo() //fixme: update and save
            val events = mutableListOf<GlucometerEventDto>()
            repeat(1000) { index -> //TODO: сюда прописать условия остановки
                val event = readEvent(index)
                val eventDto = eventBuilder.buildFrom(
                    "test@mail.ru",
                    address,
                    event,
                    glucometerInfo.glucometerSerialNumber
                )
                events.add(eventDto)
            }
            disconnectGlucometer()
            return events
        }
    }

    override suspend fun findGlucometer(address: String, pin: String) {
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
                    try { //TODO: вызывается второй раз с исключением
                        // TODO: нужен таймаут на сканирование
                        continuation.resume(result)
                    } catch (ex: IllegalStateException) {
                        scannerService.stopScan()
                    } finally {
                        scannerService.stopScan()
                    }
                }
            }
        }
    }

    suspend fun testAllCommand(address: String, pin: String) {
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