package com.elta.android.data.features.devices.glucometer.service

import android.bluetooth.le.ScanResult
import android.content.Context
import com.elta.android.common.constants.GLUCOMETER_MODEL
import com.elta.android.common.errors.BluetoothNotEnabledError
import com.elta.android.common.errors.CommandError
import com.elta.android.common.errors.GlucometerOfflineError
import com.elta.android.common.errors.GlucometerPinNotFoundInternaly
import com.elta.android.common.errors.GlucometerSyncError
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
import com.elta.android.data.features.devices.glucometer.builder.GlucometerEventBuilder
import com.elta.android.data.features.devices.glucometer.builder.GlucometerInfoBuilder
import com.elta.android.data.features.devices.glucometer.command.Commands
import com.elta.android.data.features.devices.glucometer.service.connect.ConnectService
import com.elta.android.data.features.devices.glucometer.startScan
import com.elta.android.data.features.devices.glucometer.storage.GlucometerPinStorage
import com.elta.android.data.features.diary.events.cache.EventsConditions
import com.elta.android.data.features.diary.events.cache.dto.v2.EventV2CachedDto
import com.elta.android.data.features.diary.events.dto.EventTypeDto
import com.elta.android.data.features.user.cache.dto.ProfileCacheDto
import com.elta.android.domain.features.FeatureToggles
import com.elta.android.iiot.IiotSdkDeviceService
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.exceptions.BleException
import io.reactivex.Observable
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
import org.threeten.bp.ZonedDateTime
import timber.log.Timber
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import javax.inject.Inject
import javax.inject.Singleton


private const val EVENTS_COUNT = 1000
private const val SYNC_DELAY = 1000L
private const val COMMAND_DELAY = 20L
private const val SEND_FIND_COMMAND_DELAY_MILLIS = 8000L

@Singleton
@Suppress("TooManyFunctions", "NestedBlockDepth")
class GlucometersService @Inject constructor(
    private val utilService: UtilService, //FIXME: rename для статичных переменных
    private val connectService: ConnectService,
    private val glucometersInfoToCacheMapper: Mapper<GlucometerInfoDto, GlucometerInfoCachedDto>,
    private val userHolder: UserHolder,
    private val profileCache: Cache<ProfileCacheDto>,
    private val eventsCache: Cache<EventV2CachedDto>,
    private val glucometersCache: Cache<GlucometerCachedDto>,
    private val glucometersInfoCache: Cache<GlucometerInfoCachedDto>,
    private val eventBuilder: GlucometerEventBuilder,
    private val pinStorage: GlucometerPinStorage,
    private val infoBuilder: GlucometerInfoBuilder,
    private val client: RxBleClient,
    private val context: Context
) {

    fun syncWithDevice(device: GlucometerDto?): Observable<List<GlucometerEventDto>> {
        val findDeviceAddress =
            device?.address ?: glucometersCache.get(GlucometersConditions.Primary)?.address
        return findDeviceAddress?.let { address ->
            if (client.state == RxBleClient.State.BLUETOOTH_NOT_ENABLED) {
                Observable.error(BluetoothNotEnabledError)
            } else {
                utilService.adapter.bluetoothLeScanner.startScan(utilService.filters, utilService.settings, context)
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


    @OptIn(ObsoleteCoroutinesApi::class, ExperimentalCoroutinesApi::class, FlowPreview::class)
    fun findGlucometer(address: String): Flow<Unit> =
        connectService.findConnection(address)
            .checkPinAndSend(pinStorage.getPin(address)) { connection, pin ->
                utilService.request(connection, address, Commands.SetPin(pin))

            }
            .asFlow()
            .flatMapLatest { connection ->
                ticker(delayMillis = SEND_FIND_COMMAND_DELAY_MILLIS)
                    .receiveAsFlow()
                    .flatMapMerge {
                        connection.request(
                            Commands.TurnOnFindMode,
                            pin = pinStorage.getPin(address),
                            pinErrorCallback = { pinStorage.setPin(address, "") },
                        )
                            .asFlow()
                            .map { Unit }
                    }
            }

    private fun syncInternal(address: String): Observable<List<GlucometerEventDto>> {
        var numberOfEventsRead = 0
        return utilService.checkBluetoothClientState()
            .switchMap { connectService.findConnection(address) }
            .switchMap { connection ->
                connection.setupNotification(UART_TX).map { Pair(connection, it) }
            }
            .concatMap {
                val connection = it.first
                val responses = it.second

                val pin = pinStorage.getPin(address)

                Timber.i("<<<<<<<Sync>>>>>>  Pin: $pin")

                if (pin.isNullOrEmpty()) throw GlucometerPinNotFoundInternaly

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
                    .map<Commands> { index -> Commands.ReadEvent(index) }
                    .startWith(startCommands)
                    .concatMap { command ->
                        Observable.just(command).delay(COMMAND_DELAY, TimeUnit.MILLISECONDS)
                            .concatMapSingle {
                                val input = command.getByteCommand()
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
                        Observable.error(CommandError(response))
                    } else {
                        Observable.just(response)
                    }
                }
            }
            .map { response ->
                val info =
                    glucometersInfoCache.get(CommonConditions.ById(address.hashCode().toLong()))
                val lastEvent = info?.lastSyncedEvent

                Timber.i("<<<<<<<Sync>>>>>>  Info from cache by address: $info")
                Timber.i("<<<<<<<Sync>>>>>>  LastEvent: $lastEvent")
                response to lastEvent
            }
            .takeUntil { (response, lastEvent) ->
                if (response.isEvent()) numberOfEventsRead++
                response.isEmptyEvent() || response == lastEvent || numberOfEventsRead == EVENTS_COUNT
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
                            serial = glucometersInfoCache.get(
                                CommonConditions.ById(
                                    address.hashCode().toLong()
                                )
                            )?.glucometerSerialNumber.orEmpty(),
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
            .map { glucometerEvents ->
                val events = glucometerEvents.map {
                    GlucometerEventDto(
                        id = it.id,
                        date = it.date,
                        temperature = it.temperature,
                        value = it.value,
                        glucometerSerialNumber = it.glucometerSerialNumber,
                        originalResponse = it.originalResponse
                    )
                }
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

    private fun getCachedEvents(fromGlucometer: List<GlucometerEventDto>): List<EventV2CachedDto> =
        eventsCache.getAll(
            EventsConditions.ByTypeAndIds(
                EventTypeDto.GLUCOSE,
                fromGlucometer.map { it.id.hashCode().toLong() }.toLongArray()
            )
        )

    private fun filterExistingEvents(
        fromGlucometer: List<GlucometerEventDto>,
        cached: List<EventV2CachedDto>
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
