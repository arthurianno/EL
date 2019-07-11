package com.elta.android.data.features.devices.glucometer

import android.content.Context
import com.elta.android.common.errors.BluetoothNotAvailableError
import com.elta.android.common.errors.BluetoothNotEnabledError
import com.elta.android.common.errors.CommandError
import com.elta.android.common.errors.FirmwareNotSupportedByAppError
import com.elta.android.common.errors.GlucometerLowBatteryLevelError
import com.elta.android.common.errors.GlucometerOfflineError
import com.elta.android.common.errors.GlucometerPinIncorrectOrNotFoundError
import com.elta.android.common.errors.GlucometerPinRequireError
import com.elta.android.common.errors.GlucometerSyncError
import com.elta.android.common.errors.GlucometerToDfuModeError
import com.elta.android.common.errors.LocationNotEnabledError
import com.elta.android.common.errors.LocationPermissionNotGrantedError
import com.elta.android.common.errors.PrimaryGlucometerNotFoundError
import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.common.cache.Cache
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.common.storage.UserHolder
import com.elta.android.data.features.devices.cache.GlucometersConditions
import com.elta.android.data.features.devices.cache.dto.GlucometerCachedDto
import com.elta.android.data.features.devices.cache.dto.GlucometerInfoCachedDto
import com.elta.android.data.features.devices.dto.GlucometerDto
import com.elta.android.data.features.devices.dto.GlucometerEventDto
import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import com.elta.android.data.features.diary.events.cache.EventsConditions
import com.elta.android.data.features.diary.events.cache.dto.EventCachedDto
import com.elta.android.data.features.diary.events.dto.EventTypeDto
import com.elta.android.data.features.user.cache.dto.ProfileCacheDto
import com.elta.android.domain.features.firmware.model.Firmware
import com.elta.android.domain.features.firmware.model.FirmwareFile
import com.jakewharton.rx.ReplayingShare
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.exceptions.BleDisconnectedException
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanFilter
import no.nordicsemi.android.support.v18.scanner.ScanResult
import no.nordicsemi.android.support.v18.scanner.ScanSettings
import org.threeten.bp.ZonedDateTime
import java.nio.charset.Charset
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Suppress("TooManyFunctions", "NestedBlockDepth")
class GlucometersManager @Inject constructor(
    private val glucometersInfoToCacheMapper: Mapper<GlucometerInfoDto, GlucometerInfoCachedDto>,
    private val glucometersInfoFromCacheMapper: Mapper<GlucometerInfoCachedDto, GlucometerInfoDto>,
    private val glucometerFromCacheMapper: Mapper<GlucometerCachedDto, GlucometerDto>,
    private val glucometerToCacheMapper: Mapper<GlucometerDto, GlucometerCachedDto>,
    private val userHolder: UserHolder,
    private val profileCache: Cache<ProfileCacheDto>,
    private val eventsCache: Cache<EventCachedDto>,
    private val glucometersCache: Cache<GlucometerCachedDto>,
    private val glucometersInfoCache: Cache<GlucometerInfoCachedDto>,
    private val eventBuilder: GlucometerEventBuilder,
    private val pinStorage: GlucometerPinStorage,
    private val infoBuilder: GlucometerInfoBuilder,
    private val client: RxBleClient,
    private val context: Context
) {

    private val scanner = BluetoothLeScannerCompat.getScanner()

    private val settings = ScanSettings.Builder()
        .setLegacy(false)
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .setReportDelay(0)
        .setUseHardwareBatchingIfSupported(true)
        .build()

    private val filters = listOf<ScanFilter>(
        ScanFilter.Builder().setDeviceName("Satellite").build()
    )

    private val connections = mutableMapOf<String, RxBleConnection>()
    private val infoCommands = listOf(
        Commands.GetDate, Commands.GetBatteryAndTemperature, Commands.GetVersion
    )

    fun isSupportedByApplication(firmware: Firmware): Boolean = isSupported(firmware.compatible)

    fun isSupportedByApplication(firmwareFile: FirmwareFile): Boolean = isSupported(firmwareFile.compatible)

    fun findDevices(): Observable<List<ScanResult>> =
        Observable.just(client.state)
            .flatMap { state ->
                val error = state.toError()
                if (error != null) Observable.error(error)
                else Observable.just(state)
            }
            .flatMap {
                val connectedDevices = glucometersCache.getAll(CommonConditions.All)
                scanner.startScan(filters, settings)
                    .map { filterConnectedDevices(connectedDevices, it) }
            }

    fun getDevices(): Single<List<GlucometerDto>> =
        Single.just(glucometersCache.getAll(CommonConditions.All))
            .map(glucometerFromCacheMapper::mapFromObjects)

    fun getDevice(address: String): Single<GlucometerDto> =
        Single.just(glucometersCache.get(CommonConditions.ById(address.hashCode().toLong())))
            .map(glucometerFromCacheMapper::mapFromObject)

    fun deleteDevice(address: String): Completable {
        val id = address.hashCode().toLong()
        return Single.just(glucometersCache.get(CommonConditions.ById(id)))
            .doOnSuccess {
                glucometersCache.delete(CommonConditions.ById(id))
                glucometersInfoCache.delete(CommonConditions.ById(id))
            }
            .filter { it.isPrimary }
            .map { glucometersInfoCache.getAll(CommonConditions.All) }
            .filter { it.isNotEmpty() }
            .map { glucometers -> glucometers.sortedByDescending { it.syncDate }.first() }
            .map { glucometersCache.get(CommonConditions.ById(it.id)) }
            .map { it.copy(isPrimary = true) }
            .map { glucometersCache.update(listOf(it)) }
            .ignoreElement()
    }

    fun getGlucometerInfo(address: String): Single<GlucometerInfoDto> =
        client.findConnection(address)
            .checkPinAndSend(address)
            .switchMap { connection ->
                connection.batchRequest(address, infoCommands)
            }
            .take(1)
            .map { infoBuilder.buildFrom(address, it) }
            .singleOrError()

    fun getLastGlucometerInfo(address: String): Single<GlucometerInfoDto> =
        Single.fromCallable {
            val id = address.hashCode().toLong()
            glucometersInfoCache.get(CommonConditions.ById(id))
                ?: GlucometerInfoCachedDto(id = id, secondaryId = address)
        }.map(glucometersInfoFromCacheMapper::mapFromObject)

    fun getGlucometerEvents(address: String): Single<List<GlucometerEventDto>> =
        client.findConnection(address)
            .checkPinAndSend(address)
            .switchMap { connection ->
                Observable.range(0, EVENTS_COUNT)
                    .concatMap {
                        connection.request(address, Commands.ReadEvent(it))
                    }
            }
            .takeUntil { it.isEmptyEvent() }
            .collectInto(mutableListOf<String>()) { responses, response ->
                if (!response.isEmptyEvent()) responses.add(response)
            }
            .map { events ->
                userHolder.currentUser?.let { id ->
                    profileCache.get(CommonConditions.ById(id))?.let { profile ->
                        profile.email?.let { userId ->
                            events.map { event -> eventBuilder.buildFrom(userId, address, event) }
                        }
                    }
                } ?: emptyList()
            }

    fun connectDevice(device: GlucometerDto, pinCode: String): Completable =
        client.findConnection(device.address)
            .switchMap { connection ->
                connection.simpleRequest(device.address, Commands.SetPin(pinCode))
            }
            .take(1)
            .switchMapCompletable { response ->
                when {
                    response.isPinError() -> Completable.error(GlucometerPinIncorrectOrNotFoundError)
                    else -> Completable.fromCallable {
                        pinStorage.setPin(device.address, pinCode)
                        val primaryDevice = glucometersCache.get(GlucometersConditions.Primary)
                        val newDevice = glucometerToCacheMapper.mapFromObject(device)

                        if (primaryDevice == null) {
                            glucometersCache.add(listOf(newDevice.apply { isPrimary = true }))
                        }

                        if (primaryDevice != null && !primaryDevice.address.equals(device.address, true)) {
                            glucometersCache.add(listOf(newDevice))
                        }
                    }
                }
            }

    fun syncWithDevice(device: GlucometerDto?): Observable<List<GlucometerEventDto>> =
        device?.let {
            Observable.just(Unit)
                .delay(SYNC_DELAY, TimeUnit.MILLISECONDS)
                .flatMap { syncInternal(device.address) }
        }
            ?: glucometersCache.get(GlucometersConditions.Primary)?.let { syncInternal(it.address) }
            ?: Observable.error(PrimaryGlucometerNotFoundError)

    fun updateFirmware(address: String, file: FirmwareFile): Completable =
        when {
            !isSupportedByApplication(file) -> Completable.error(FirmwareNotSupportedByAppError(file.version))
            else ->
                checkBluetoothClientState()
                    .flatMapCompletable {
                        client.findConnection(address)
                            .checkPinAndSend(address)
                            .switchMap { connection ->
                                connection.request(address, Commands.GetBatteryAndTemperature)
                                    .map { infoBuilder.buildFrom(address, listOf(it)) }
                                    .switchMap { info ->
                                        when {
                                            !info.isBatteryLevelEnoughForUpdate() -> Observable.error(
                                                GlucometerLowBatteryLevelError(
                                                    current = info.batteryLevel ?: 0,
                                                    required = MIN_LEVEL
                                                )
                                            )
                                            else -> connection.request(address, Commands.ToDfuMode)
                                        }
                                    }
                            }
                            .take(1)
                            .switchMapCompletable { response ->
                                when (response.isOk()) {
                                    true -> startFirmwareUpdate(context, file.path, address.toDfuAddress())
                                    else -> Completable.error(GlucometerToDfuModeError)
                                }
                            }
                            // we can't know when device will completely reboot after update
                            // to get actual info so we using this this hack to update glucometer
                            // version after update firmware.
                            .doOnComplete {
                                val id = address.hashCode().toLong()
                                glucometersInfoCache.get(CommonConditions.ById(id))?.let { info ->
                                    file.version.toDoubleOrNull()?.let { version ->
                                        val newInfo = info.copy(software = version)
                                        glucometersInfoCache.update(listOf(newInfo))
                                    }
                                }
                            }
                    }
        }

    fun setPrimaryDevice(address: String): Completable =
        Completable.fromCallable {
            val glucometers = glucometersCache.getAll(CommonConditions.All)
            var oldPrimaryGlucometer: GlucometerCachedDto? = null
            var newPrimaryGlucometer: GlucometerCachedDto? = null
            glucometers.forEach {
                when {
                    it.isPrimary -> oldPrimaryGlucometer = it.copy(isPrimary = false)
                    it.address == address -> newPrimaryGlucometer = it.copy(isPrimary = true)
                }
            }
            val glucometersToUpdate = mutableListOf<GlucometerCachedDto>().apply {
                oldPrimaryGlucometer?.let { add(it) }
                newPrimaryGlucometer?.let { add(it) }
            }.toList()
            if (glucometersToUpdate.isNotEmpty()) glucometersCache.update(glucometersToUpdate)
        }

    private fun RxBleConnection.simpleRequest(address: String, cmd: GlucometerCommand): Observable<String> {
        val input = cmd.toGlucometerString()
        val notification = setupNotification(UART_TX)
            .switchMap { it }
            .map { it.toString(Charset.defaultCharset()) }
        val command = writeCharacteristic(UART_RX, input.toByteArray(Charset.defaultCharset()))
            .toObservable().map { it.toString(Charset.defaultCharset()) }
        return Observables.combineLatest(notification.take(1), command) { response, _ -> response }
    }

    private fun RxBleConnection.request(address: String, cmd: GlucometerCommand): Observable<String> {
        val input = cmd.toGlucometerString()
        val notification = setupNotification(UART_TX)
            .switchMap { it }
            .map { it.toString(Charset.defaultCharset()) }
        val command = writeCharacteristic(UART_RX, input.toByteArray(Charset.defaultCharset()))
            .toObservable().map { it.toString(Charset.defaultCharset()) }
        return Observables.combineLatest(notification.take(1), command) { response, _ -> response }
            .take(1)
            .compose {
                it.switchMap { response ->
                    when {
                        input.isPinCommand() && response.isPinError() -> {
                            pinStorage.setPin(address, "")
                            Observable.error(GlucometerPinIncorrectOrNotFoundError)
                        }
                        response.isPinError() -> Observable.error(GlucometerPinRequireError)
                        else -> Observable.just(response)
                    }
                }
            }
            .retryWhen { errors ->
                errors
                    .concatMap {
                        when (it) {
                            is GlucometerPinRequireError -> Observable.just(Unit)
                            else -> Observable.error(it)
                        }
                    }
                    .concatMap {
                        val pin = pinStorage.getPin(address)
                        when (pin.isNullOrEmpty()) {
                            true -> Observable.error(GlucometerPinIncorrectOrNotFoundError)
                            else -> request(address, Commands.SetPin(pin))
                        }
                    }
            }
    }

    private fun RxBleConnection.batchRequest(
        address: String,
        commands: List<GlucometerCommand>
    ): Observable<List<String>> =
        Observable.fromIterable(commands.map { cmd -> request(address, cmd) })
            .concatMap { it }
            .buffer(commands.size)

    private fun RxBleClient.findConnection(address: String): Observable<RxBleConnection> =
        Observable.just(getBleDevice(address))
            .switchMap { device ->
                val connection = connections[address]
                if (connection == null || device.connectionState == RxBleConnection.RxBleConnectionState.DISCONNECTED)
                    device.establishConnection(false)
                        .onErrorResumeNext { e: Throwable ->
                            if (e is BleDisconnectedException) Observable.error(GlucometerOfflineError)
                            else Observable.error(e)
                        }
                        .compose(ReplayingShare.instance())
                        .doOnNext { connections[address] = it }
                else Observable.just(connection)
            }

    private fun Observable<RxBleConnection>.checkPinAndSend(address: String): Observable<RxBleConnection> =
        this.switchMap { connection ->
            val pin = pinStorage.getPin(address)
            when (pin.isNullOrEmpty()) {
                true -> Observable.error(GlucometerPinIncorrectOrNotFoundError)
                else -> connection.request(address, Commands.SetPin(pin))
                    .switchMap { Observable.just(connection) }
            }
        }

    private inline fun String.isPinError(): Boolean = this == "pin.error"
    private inline fun String.isPinCommand(): Boolean = startsWith("pin")
    private inline fun String.isEmptyEvent(): Boolean = contains("rd000000000000000000")
    private inline fun String.isOk(): Boolean = endsWith("ok")
    private inline fun String.isError(): Boolean = contains("error")
    private inline fun String.isEvent(): Boolean = startsWith("rd")
    private inline fun isSupported(compatible: String): Boolean {
        val appVersionCode = FIRMWARE_VERSION.replace(".", "").toInt()
        val compatibleVersionCode = compatible.replace(".", "").toInt()
        return appVersionCode >= compatibleVersionCode
    }

    private fun GlucometerInfoDto.isBatteryLevelEnoughForUpdate(): Boolean = batteryLevel ?: 0 >= MIN_LEVEL

    private fun RxBleClient.State.toError(): Throwable? =
        when (this) {
            RxBleClient.State.BLUETOOTH_NOT_AVAILABLE -> BluetoothNotAvailableError
            RxBleClient.State.BLUETOOTH_NOT_ENABLED -> BluetoothNotEnabledError
            RxBleClient.State.LOCATION_PERMISSION_NOT_GRANTED -> LocationPermissionNotGrantedError
            RxBleClient.State.LOCATION_SERVICES_NOT_ENABLED -> LocationNotEnabledError
            else -> null
        }

    private fun syncInternal(address: String): Observable<List<GlucometerEventDto>> =
        checkBluetoothClientState()
            .switchMap { client.findConnection(address) }
            .switchMap { connection -> connection.setupNotification(UART_TX).map { Pair(connection, it) } }
            .concatMap {
                val connection = it.first
                val responses = it.second

                val pin = pinStorage.getPin(address)
                if (pin.isNullOrEmpty()) throw GlucometerPinIncorrectOrNotFoundError

                val startCommands = mutableListOf(Commands.SetPin(pin), Commands.SetTime(ZonedDateTime.now()),
                    Commands.GetDate, Commands.GetBatteryAndTemperature, Commands.GetVersion
                )

                val info = glucometersInfoCache.get(CommonConditions.ById(address.hashCode().toLong()))
                val lastEvent = info?.lastSyncedEvent

                Observable.range(0, EVENTS_COUNT)
                    .map { index -> Commands.ReadEvent(index) as GlucometerCommand }
                    .startWith(startCommands)
                    .concatMap { command ->
                        Observable.just(command).delay(COMMAND_DELAY, TimeUnit.MILLISECONDS)
                            .concatMapSingle {
                                val input = command.toGlucometerString().toByteArray(Charset.defaultCharset())
                                connection.writeCharacteristic(UART_RX, input).map { Pair(responses, lastEvent) }
                            }
                    }
            }
            // Pair.first -> responses Observable<ByteArray>
            // Pair.second -> last synced event
            .concatMap { pair -> pair.first.map { Pair(it, pair.second) } }
            .compose {
                it.switchMap { pair ->
                    val bytes = pair.first
                    val response = bytes.toString(Charset.defaultCharset())
                    if (response.isError()) Observable.error(CommandError)
                    else Observable.just(Pair(response, pair.second))
                }
            }
            // Pair.first -> response
            // Pair.second -> last synced event
            .takeUntil { it.first.isEmptyEvent() || it.first == it.second }
            .collectInto(SyncResponseHolder()) { holder, pair ->
                val r = pair.first
                if (r.isEvent() && !r.isEmptyEvent() && r != pair.second) holder.events.add(r)
                else if (!r.isOk() && !r.isError() && !r.isEmptyEvent()) holder.info.add(r)
            }
            .toObservable()
            .take(1)
            // Glucometers memory organized like stack, so the most recent event will be on the top
            // or in holder if there are no new events
            .doOnNext { holder ->
                updateGlucometerInfo(address, holder.info, holder.events.firstOrNull() ?: holder.lastSyncedEvent)
            }
            .map(SyncResponseHolder::events)
            .map { events ->
                userHolder.currentUser?.let { id ->
                    profileCache.get(CommonConditions.ById(id))?.let { profile ->
                        profile.email?.let { userId ->
                            events.map { event -> eventBuilder.buildFrom(userId, address, event) }
                        }
                    }
                } ?: emptyList()
            }
            .map { events -> filterExistingEvents(events, getCachedEvents(events)) }
            .flatMap {
                if (it.isEmpty()) Observable.empty()
                else Observable.just(it)
            }
            .onErrorResumeNext { e: Throwable -> Observable.error(GlucometerSyncError(e)) }

    private fun filterConnectedDevices(
        connected: List<GlucometerCachedDto>,
        results: List<ScanResult>
    ): List<ScanResult> {
        val filtered = mutableListOf<ScanResult>()
        results.forEach { result ->
            if (connected.firstOrNull { it.address.equals(result.device.address, true) } == null) {
                filtered.add(result)
            }
        }
        return filtered
    }

    private fun checkBluetoothClientState(): Observable<RxBleClient.State> =
        Observable.just(client.state)
            .flatMap { state ->
                val error = state.toError()
                if (error != null) Observable.error(error)
                else Observable.just(state)
            }

    private fun getCachedEvents(fromGlucometer: List<GlucometerEventDto>): List<EventCachedDto> =
        eventsCache.getAll(
            EventsConditions.ByTypeAndIds(
                EventTypeDto.GLUCOSE, fromGlucometer.map { it.id.hashCode().toLong() }.toLongArray()
            )
        )

    private fun filterExistingEvents(
        fromGlucometer: List<GlucometerEventDto>,
        cached: List<EventCachedDto>
    ): List<GlucometerEventDto> =
        if (cached.isEmpty()) fromGlucometer
        else arrayListOf<GlucometerEventDto>().apply {
            fromGlucometer.forEach { event ->
                if (cached.find { it.secondaryId == event.id } == null) add(event)
            }
        }

    private fun updateGlucometerInfo(address: String, responses: List<String>, lastEvent: String?) {
        val info = infoBuilder.buildFrom(address, responses, ZonedDateTime.now(), lastEvent)
        val cachedInfo = glucometersInfoCache.get(CommonConditions.ById(address.hashCode().toLong()))
        val newInfo = glucometersInfoToCacheMapper.mapFromObject(info)
        if (cachedInfo == null) glucometersInfoCache.add(listOf(newInfo))
        else glucometersInfoCache.update(listOf(newInfo))
    }

    data class SyncResponseHolder(
        val info: MutableList<String> = mutableListOf(),
        val events: MutableList<String> = mutableListOf(),
        var lastSyncedEvent: String? = null // #GlucometerInfoCachedDto.lastSyncedEvent
    )

    companion object {
        private const val FIRMWARE_VERSION = "1.8" // version of firmware supported by application
        private const val MIN_LEVEL = 1 // minimal level of battery required to start firmware update
        private val UART_RX = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e")
        private val UART_TX = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e")
        private const val EVENTS_COUNT = 1000
        private const val SYNC_DELAY = 500L
        private const val COMMAND_DELAY = 4L
    }
}