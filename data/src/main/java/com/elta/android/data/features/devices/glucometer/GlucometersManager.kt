package com.elta.android.data.features.devices.glucometer

import android.content.Context
import com.elta.android.common.constants.GLUCOMETER_MODEL
import com.elta.android.common.errors.BluetoothNotAvailableError
import com.elta.android.common.errors.BluetoothNotEnabledError
import com.elta.android.common.errors.CommandError
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
import com.elta.android.data.features.diary.events.cache.dto.v1.EventCachedDto
import com.elta.android.data.features.diary.events.dto.EventTypeDto
import com.elta.android.data.features.user.cache.dto.ProfileCacheDto
import com.elta.android.domain.features.FeatureToggles
import com.elta.android.domain.features.firmware.model.FirmwareFile
import com.elta.android.iiot.IiotSdkDeviceService
import com.jakewharton.rx.ReplayingShare
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.exceptions.BleDisconnectedException
import com.polidea.rxandroidble2.exceptions.BleException
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.rx2.asFlow
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanFilter
import no.nordicsemi.android.support.v18.scanner.ScanResult
import no.nordicsemi.android.support.v18.scanner.ScanSettings
import org.threeten.bp.ZonedDateTime
import timber.log.Timber
import java.nio.charset.Charset
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import javax.inject.Inject
import javax.inject.Singleton

private const val MIN_BATTERY_LEVEL = 1
private val UART_RX = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e")
private val UART_TX = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e")
private const val EVENTS_COUNT = 999
private const val SYNC_DELAY = 1000L
private const val COMMAND_DELAY = 20L
private const val SEND_FIND_COMMAND_DELAY_MILLIS = 8000L

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

    private val dfuFilters = listOf<ScanFilter>(
        ScanFilter.Builder().setDeviceName("Dfu").build()
    )

    private val connections = mutableMapOf<String, RxBleConnection>()
    private val infoCommands = listOf(
        Commands.GetDate,
        Commands.GetBatteryAndTemperature,
        Commands.GetVersion,
        Commands.Serial
    )

    fun findDevices(): Observable<List<ScanResult>> =
        Observable.just(client.state)
            .flatMap { it.observableState() }
            .flatMap {
                val connectedDevices = glucometersCache.getAll(CommonConditions.All)
                scanner.startScan(filters, settings)
                    .map { filterConnectedDevices(connectedDevices, it) }
            }

    fun getDevices(): Single<List<Pair<GlucometerDto, GlucometerInfoDto>>> =
        Single.just(
            glucometersCache.getAll(CommonConditions.All)
                .map(glucometerFromCacheMapper::mapFromObject)
                .map {
                    val id = it.address.hashCode().toLong()
                    it to glucometersInfoFromCacheMapper.mapFromObject(
                        glucometersInfoCache.get(CommonConditions.ById(id))
                            ?: GlucometerInfoCachedDto(id = id, secondaryId = it.address)
                    )
                }
        )

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

    fun getGlucometerEvents(address: String): Single<List<GlucometerEventDto>> {
        val glucometerInfo =
            glucometersInfoCache.get(
                CommonConditions.ById(
                    address.hashCode().toLong()
                )
            )

        return client.findConnection(address)
            .checkPinAndSend(address)
            .switchMap { connection ->
                Observable.range(0, EVENTS_COUNT)
                    .concatMap {
                        connection.request(address, Commands.ReadEvent(it))
                    }
            }
            .takeUntil { it.isEmptyEvent() }
            .doOnNext { event ->
                if (FeatureToggles.isEnableIiotSdkFeature && event.isEvent() && !event.isEmptyEvent()) {
                    IiotSdkDeviceService.sendEvent(
                        event = eventBuilder.getTimeAndValue(event),
                        serial = glucometerInfo?.glucometerSerialNumber.orEmpty(),
                        model = GLUCOMETER_MODEL
                    )
                }
            }
            .collectInto(mutableListOf<String>()) { responses, response ->
                if (!response.isEmptyEvent()) {
                    responses.add(response)
                }
            }
            .map { events ->
                events.forEach {
                    Timber.i("<<<<<<< getGlucometerEvents >>>>>>  Response : $it")
                }
                userHolder.currentUser?.let { id ->
                    profileCache.get(CommonConditions.ById(id))?.let { profile ->
                        profile.email?.let { userId ->
                            events.map { event ->
                                eventBuilder.buildFrom(
                                    userId = userId,
                                    glucometerId = address,
                                    response = event,
                                    glucometerSerialNumber = glucometerInfo?.glucometerSerialNumber
                                )
                            }
                        }
                    }
                } ?: emptyList()
            }
    }

    fun connectDevice(device: GlucometerDto, pinCode: String): Completable =
        client.findConnection(device.address)
            .switchMap { connection ->
                connection.simpleRequest(device.address, Commands.SetPin(pinCode))
            }
            .take(1)
            .switchMapCompletable { response ->
                when {
                    response.isPinError() -> Completable.error(
                        GlucometerPinIncorrectOrNotFoundError
                    )

                    else -> Completable.fromCallable {
                        pinStorage.setPin(device.address, pinCode)
                        val primaryDevice = glucometersCache.get(GlucometersConditions.Primary)
                        val newDevice = glucometerToCacheMapper.mapFromObject(device)

                        if (primaryDevice == null) {
                            glucometersCache.add(listOf(newDevice.apply { isPrimary = true }))
                        }

                        if (primaryDevice != null && !primaryDevice.address.equals(
                                device.address,
                                true
                            )
                        ) {
                            glucometersCache.add(listOf(newDevice))
                        }
                    }
                }
            }

    fun syncWithDevice(device: GlucometerDto?): Observable<List<GlucometerEventDto>> {
        val findDeviceAddress =
            device?.address ?: glucometersCache.get(GlucometersConditions.Primary)?.address
        return findDeviceAddress?.let { address ->
            if (client.state == RxBleClient.State.BLUETOOTH_NOT_ENABLED) {
                Observable.error(BluetoothNotEnabledError)
            } else {
                scanner.startScan(filters, settings)
                    .filter { findGlucometers ->
                        findGlucometers.map { it.device.address }
                            .contains(address)
                    }
                    .take(1)
                    .doOnNext { Timber.i("<<<<<<<StartSyncInternal>>>>>>  ScanResults: $it") }
                    .flatMap {
                        Observable.just(Unit)
                            .delay(SYNC_DELAY, TimeUnit.MILLISECONDS)
                            .flatMap { syncInternal(address) }
                    }
            }
        }
            ?.onErrorResumeNext { exception: Throwable ->
                Timber.i("<<<<<<<syncWithDeviceError>>>>>>  syncWithDevice: $exception")
                val error = when (exception) {
                    is BleException -> GlucometerOfflineError
                    is TimeoutException -> GlucometerSyncError(exception)
                    else -> exception
                }
                Observable.error(error)
            }
            ?: Observable.error(PrimaryGlucometerNotFoundError)
    }

    fun updateFirmware(address: String, file: FirmwareFile): Observable<String> =
        checkBluetoothClientState()
            .flatMap {
                client.findConnection(address)
                    .checkPinAndSend(address)
                    .switchMap { connection ->
                        connection.request(address, Commands.GetBatteryAndTemperature)
                            .map { infoBuilder.buildFrom(address, listOf(it)) }
                            .switchMap { info ->
                                checkBattery(info, connection, address)
                            }
                    }
                        .take(1)
                        .switchMap { response ->
                            firmwareUpdate(response, address, file)
                        }
                    // we can't know when device will completely reboot after update
                    // to get actual info so we using this this hack to update glucometer
                    // version after update firmware.
                    .doOnComplete {
                        val id = address.hashCode().toLong()
                        glucometersInfoCache.get(CommonConditions.ById(id))?.let { info ->
                            glucometersInfoCache.update(
                                listOf(info.copy(software = file.version))
                            )
                        }
                    }
            }

    private fun firmwareUpdate(
        response: String,
        address: String,
        file: FirmwareFile
    ): Observable<String> {
        return if (response.isOk()) {
            val dfuAddress = address.toDfuAddress()
            scanner.startScan(dfuFilters, settings)
                .filter { results -> results.map { it.device.address }.contains(dfuAddress) }
                .take(1)
                .switchMap { startFirmwareUpdate(context, file.path, dfuAddress) }
        } else {
            Observable.error(GlucometerToDfuModeError)
        }
    }

    private fun checkBattery(
        info: GlucometerInfoDto,
        connection: RxBleConnection,
        address: String
    ) = when {
        !info.isBatteryLevelEnoughForUpdate() -> Observable.error(
            GlucometerLowBatteryLevelError(
                current = info.batteryLevel ?: 0,
                required = MIN_BATTERY_LEVEL
            )
        )

        else -> connection.request(address, Commands.ToDfuMode)
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

    private fun RxBleClient.State.observableState(): ObservableSource<out RxBleClient.State> =
        toError()?.let { Observable.error(it) }
            ?: Observable.just(this)

    private fun RxBleConnection.simpleRequest(
        address: String,
        cmd: GlucometerCommand
    ): Observable<String> {
        val input = cmd.toGlucometerString()
        val notification = setupNotification(UART_TX)
            .switchMap { it }
            .map { it.toString(Charset.defaultCharset()) }
        val command = writeCharacteristic(UART_RX, input.toByteArray(Charset.defaultCharset()))
            .toObservable().map { it.toString(Charset.defaultCharset()) }
        return Observables.combineLatest(notification.take(1), command) { response, _ -> response }
    }

    private fun RxBleConnection.request(
        address: String,
        cmd: GlucometerCommand
    ): Observable<String> {
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
                if (connection == null || device.connectionState == RxBleConnection.RxBleConnectionState.DISCONNECTED) {
                    device.establishConnection(false)
                        .onErrorResumeNext { throwable: Throwable ->
                            Timber.e(throwable, javaClass.simpleName, throwable.message)
                            when {
                                client.state == RxBleClient.State.BLUETOOTH_NOT_ENABLED -> {
                                    Observable.error(BluetoothNotEnabledError)
                                }

                                throwable is BleDisconnectedException -> {
                                    Timber.i("<<<<<<<findConnectionError>>>>>>  findConnection: $throwable")
                                    Observable.error(GlucometerOfflineError)
                                }

                                else -> Observable.error(throwable)
                            }
                        }
                        .compose(ReplayingShare.instance())
                        .doOnNext { connections[address] = it }
                } else {
                    Observable.just(connection)
                }
            }

    private fun Observable<RxBleConnection>.checkPinAndSend(address: String): Observable<RxBleConnection> =
        this.switchMap { connection ->
            val pin = pinStorage.getPin(address)
            if (pin.isNullOrEmpty()) {
                Observable.error(GlucometerPinIncorrectOrNotFoundError)
            } else {
                connection.request(address, Commands.SetPin(pin))
                    .switchMap { Observable.just(connection) }
            }
        }

    private fun String.isPinError(): Boolean = this == "pin.error"
    private fun String.isPinCommand(): Boolean = startsWith("pin")
    private fun String.isEmptyEvent(): Boolean = contains("rd000000000000000000")
    private fun String.isOk(): Boolean = endsWith("ok")
    private fun String.isError(): Boolean = contains("error")
    private fun String.isEvent(): Boolean = startsWith("rd")

    private fun GlucometerInfoDto.isBatteryLevelEnoughForUpdate(): Boolean =
        (batteryLevel ?: 0) >= MIN_BATTERY_LEVEL

    private fun RxBleClient.State.toError(): Throwable? =
        when (this) {
            RxBleClient.State.BLUETOOTH_NOT_AVAILABLE -> BluetoothNotAvailableError
            RxBleClient.State.BLUETOOTH_NOT_ENABLED -> BluetoothNotEnabledError
            RxBleClient.State.LOCATION_PERMISSION_NOT_GRANTED -> LocationPermissionNotGrantedError
            RxBleClient.State.LOCATION_SERVICES_NOT_ENABLED -> LocationNotEnabledError
            else -> null
        }

    @OptIn(ObsoleteCoroutinesApi::class, ExperimentalCoroutinesApi::class, FlowPreview::class)
    fun findGlucometer(address: String): Flow<Unit> =
        client.findConnection(address)
            .checkPinAndSend(address)
            .asFlow()
            .flatMapLatest { connection ->
                ticker(delayMillis = SEND_FIND_COMMAND_DELAY_MILLIS)
                    .receiveAsFlow()
                    .flatMapMerge {
                        connection.request(address, Commands.TurnOnFindMode)
                            .asFlow()
                            .map { Unit }
                    }
            }

    private fun syncInternal(address: String): Observable<List<GlucometerEventDto>> =
        checkBluetoothClientState()
            .switchMap { client.findConnection(address) }
            .switchMap { connection ->
                connection.setupNotification(UART_TX).map { Pair(connection, it) }
            }
            .concatMap {
                val connection = it.first
                val responses = it.second

                val pin = pinStorage.getPin(address)

                Timber.i("<<<<<<<Sync>>>>>>  Pin: $pin")

                if (pin.isNullOrEmpty()) throw GlucometerPinIncorrectOrNotFoundError

                val startCommands = mutableListOf(
                    Commands.SetPin(pin),
                    Commands.SetTime(ZonedDateTime.now()),
                    Commands.GetDate,
                    Commands.GetBatteryAndTemperature,
                    Commands.GetVersion,
                    Commands.Serial
                )

                Timber.i("<<<<<<<Sync>>>>>>  Address: $address")

                Observable.range(0, EVENTS_COUNT)
                    .map<GlucometerCommand> { index -> Commands.ReadEvent(index) }
                    .startWith(startCommands)
                    .concatMap { command ->
                        Observable.just(command).delay(COMMAND_DELAY, TimeUnit.MILLISECONDS)
                            .concatMapSingle {
                                val input = command.toGlucometerString()
                                    .toByteArray(Charset.defaultCharset())
                                connection.writeCharacteristic(UART_RX, input)
                                    .map { responses }
                            }
                    }
            }
            .concatMap { responses -> responses }
            .compose {
                it.switchMap { bytes ->
                    val response = bytes.toString(Charset.defaultCharset())

                    Timber.i("<<<<<<<Sync>>>>>>  Response: $response")

                    if (response.isError()) {
                        Observable.error(CommandError)
                    } else {
                        Observable.just(response)
                    }
                }
            }
            .map { response ->
                val info = glucometersInfoCache.get(CommonConditions.ById(address.hashCode().toLong()))
                val lastEvent = info?.lastSyncedEvent

                Timber.i("<<<<<<<Sync>>>>>>  Info from cache by address: $info")
                Timber.i("<<<<<<<Sync>>>>>>  LastEvent: $lastEvent")
                response to lastEvent
            }
            .take(EVENTS_COUNT.toLong())
            .takeUntil { (response, lastEvent) ->
                response.isEmptyEvent() || response == lastEvent
            }
            .collectInto(SyncResponseHolder()) { holder, (response, lastEvent) ->
                holder.lastSyncedEvent = lastEvent
                if (response.isEvent() && !response.isEmptyEvent() && response != lastEvent) {
                    holder.events.add(response)
                } else if (!response.isOk() && !response.isError() && !response.isEmptyEvent()) {
                    holder.info.add(response)
                }
            }
            .toObservable()
            .take(1)
            .doOnNext { holder ->
                updateGlucometerInfo(
                    address,
                    holder.info,
                    holder.events.firstOrNull() ?: holder.lastSyncedEvent
                )
            }
            .doOnNext { holder ->
                if (FeatureToggles.isEnableIiotSdkFeature) {
                    holder.events.forEach { event ->
                        IiotSdkDeviceService.sendEvent(
                            event = eventBuilder.getTimeAndValue(event),
                            serial = glucometersInfoCache.get(CommonConditions.ById(address.hashCode().toLong()))?.glucometerSerialNumber.orEmpty(),
                            model = GLUCOMETER_MODEL
                        )
                    }
                }
            }
            .map(SyncResponseHolder::events)
            .map { events ->
                val glucometerInfo =
                    glucometersInfoCache.get(CommonConditions.ById(address.hashCode().toLong()))

                val userId = userHolder.currentUser
                if (userId != null) {

                    Timber.i("<<<<<<<Sync>>>>>>  currentUser: $userId")
                    val profile = profileCache.get(CommonConditions.ById(userId))

                    if (profile != null) {
                        Timber.i("<<<<<<<Sync>>>>>>  profile.email: ${profile.email}")
                        profile.email?.let { email ->
                            events.map { event ->
                                eventBuilder.buildFrom(
                                    email,
                                    address,
                                    event,
                                    glucometerInfo?.glucometerSerialNumber
                                )
                            }
                        }
                    } else {
                        emptyList()
                    }

                } else {
                    emptyList()
                }
            }
            .map { events ->
                Timber.i("<<<<<<<Sync>>>>>>  events: $events")
                val filterExistingEvents = filterExistingEvents(events, getCachedEvents(events))
                Timber.i("<<<<<<<Sync>>>>>>  filterExistingEvents: $events")
                filterExistingEvents
            }
            .flatMap {
                if (it.isEmpty()) Observable.empty() else Observable.just(it)
            }
            .onErrorResumeNext { exception: Throwable ->
                Timber.e(exception, "<<<<<<<Sync>>>>>> error ${exception.message}")
                if (exception is BleException) {
                    Timber.i("<<<<<<<syncInternalError>>>>>>  syncInternal: $exception")
                    Observable.error(GlucometerSyncError(GlucometerOfflineError))
                } else {
                    Observable.error(GlucometerSyncError(exception))
                }
            }

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
            .flatMap { it.observableState() }

    private fun getCachedEvents(fromGlucometer: List<GlucometerEventDto>): List<EventCachedDto> =
        eventsCache.getAll(
            EventsConditions.ByTypeAndIds(
                EventTypeDto.GLUCOSE,
                fromGlucometer.map { it.id.hashCode().toLong() }.toLongArray()
            )
        )

    private fun filterExistingEvents(
        fromGlucometer: List<GlucometerEventDto>,
        cached: List<EventCachedDto>
    ): List<GlucometerEventDto> =
        if (cached.isEmpty()) {
            fromGlucometer
        } else {
            arrayListOf<GlucometerEventDto>().apply {
                fromGlucometer.forEach { event ->
                    if (cached.find { it.secondaryId == event.id } == null) add(event)
                }
            }
        }

    private fun updateGlucometerInfo(address: String, responses: List<String>, lastEvent: String?) {
        Timber.i("<<<<<<<Sync>>>>>> update -  Address: $address")
        Timber.i("<<<<<<<Sync>>>>>> update - Responses: $responses")
        Timber.i("<<<<<<<Sync>>>>>> update - LastEvent: $lastEvent")
        val info = infoBuilder.buildFrom(address, responses, ZonedDateTime.now(), lastEvent)
        val cachedInfo =
            glucometersInfoCache.get(CommonConditions.ById(address.hashCode().toLong()))
        Timber.i("<<<<<<<Sync>>>>>> update -  CachedInfo: $cachedInfo")
        val newInfo = glucometersInfoToCacheMapper.mapFromObject(info)
        Timber.i("<<<<<<<Sync>>>>>> update -  NewInfo: $newInfo")
        if (cachedInfo == null) {
            Timber.i("<<<<<<<Sync>>>>>> update -  Add Info: $newInfo")
            glucometersInfoCache.add(listOf(newInfo))
        } else {
            Timber.i("<<<<<<<Sync>>>>>> update -  Update Info: $newInfo")
            glucometersInfoCache.update(listOf(newInfo))
        }
    }

    data class SyncResponseHolder(
        val info: MutableList<String> = mutableListOf(),
        val events: MutableList<String> = mutableListOf(),
        var lastSyncedEvent: String? = null // #GlucometerInfoCachedDto.lastSyncedEvent
    )
}
