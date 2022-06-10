package com.elta.android.presentation.features.devices.firmware.pm

import com.elta.android.common.errors.BluetoothNotEnabledError
import com.elta.android.common.errors.FirmwareDownloadingError
import com.elta.android.common.errors.FirmwareNotSupportedByAppError
import com.elta.android.common.errors.FirmwareUpdateError
import com.elta.android.common.errors.GlucometerLowBatteryLevelError
import com.elta.android.common.errors.GlucometerOfflineError
import com.elta.android.common.errors.LocationNotEnabledError
import com.elta.android.common.errors.LocationPermissionNotGrantedError
import com.elta.android.domain.features.devices.interactor.GetLastGlucometerInfoUseCase
import com.elta.android.domain.features.devices.interactor.UpdateDeviceFirmwareUseCase
import com.elta.android.domain.features.devices.interactor.isFirmwareNewer
import com.elta.android.domain.features.devices.model.GlucometerInfo
import com.elta.android.domain.features.firmware.interactor.GetFirmwareInfoUseCase
import com.elta.android.domain.features.firmware.interactor.GetFirmwareUseCase
import com.elta.android.domain.features.firmware.model.Firmware
import com.elta.android.domain.features.firmware.model.FirmwareFile
import com.elta.android.presentation.Events
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.sync.control.bluetoothControl2
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.Consumer
import me.dmdev.rxpm.action
import me.dmdev.rxpm.skipWhileInProgress
import me.dmdev.rxpm.state
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Suppress("TooManyFunctions")
class FirmwarePm @Inject constructor(
    private val getLastGlucometerInfoUseCase: GetLastGlucometerInfoUseCase,
    private val getFirmwareInfoUseCase: GetFirmwareInfoUseCase,
    private val getFirmwareUseCase: GetFirmwareUseCase,
    private val updateDeviceFirmwareUseCase: UpdateDeviceFirmwareUseCase,
    services: ServiceFacade
) : BasePm(services) {

    val buttonAction = action<Unit>()
    val updateState = state<UpdateState>(UpdateState.Progress(resources))

    val btControl = bluetoothControl2()

    private val getDeviceInfoAction = action<String>()
    private val checkUpdatesAction = action<Unit>()
    private val startUpdateAction = action<Unit>()
    private val downloadFirmwareAction = action<Unit>()

    private val deviceAddressState = state<String>()
    private val deviceInfo = state<GlucometerInfo>()
    private val firmwareState = state<Firmware>()
    private val firmwareFileState = state<FirmwareFile>()

    private val delayedSetStateAction = action<UpdateState>()

    override fun onCreate() {
        super.onCreate()

        bindStateBehavior()
        bindActions()
    }

    override fun handleError(error: Throwable) {
        when (error) {
            is GlucometerLowBatteryLevelError -> setState(UpdateState.BatteryLowLevel(resources))
            is FirmwareNotSupportedByAppError -> setState(
                UpdateState.UnsupportedFirmwareVersion(
                    resources
                )
            )
            is FirmwareDownloadingError -> setState(UpdateState.FirmwareDownloadingError(resources))
            is FirmwareUpdateError -> setState(UpdateState.FirmwareUpdateError(resources))
            is GlucometerOfflineError -> setState(UpdateState.GlucometerOfflineError(resources))
            else -> super.handleError(error)
        }
    }

    fun setDeviceAddress(address: String) {
        deviceAddressState.consumer.accept(address)
        getDeviceInfoAction.consumer.accept(address)
    }

    private fun bindStateBehavior() {
        delayedSetStateAction.observable
            .concatMap {
                val delay =
                    if (it is UpdateState.Progress || updateState.valueOrNull.hasUserInput()) ZERO_DELAY
                    else NEXT_STATE_DELAY
                Observable.just(it).delay(delay, TimeUnit.MILLISECONDS)
            }
            .subscribe(updateState.consumer)
            .untilDestroy()

        updateState.observable
            .filter { it is UpdateState.Updated }
            .delay(NEXT_STATE_DELAY, TimeUnit.MILLISECONDS)
            .subscribe {
                bus.event(Events.FirmwareUpdated)
                router.exit()
            }
            .untilDestroy()

        buttonAction.observable
            .subscribe {
                when (updateState.value) {
                    is UpdateState.NotFound -> checkUpdatesAction.consumer.accept(Unit)
                    is UpdateState.Found -> {
                        if (firmwareState.valueOrNull?.isCompatibleWithApplication == true) {
                            downloadFirmwareAction.consumer.accept(Unit)
                        } else {
                            setState(UpdateState.UnsupportedFirmwareVersion(resources))
                        }
                    }
                    is UpdateState.BatteryLowLevel -> router.exit()
                    is UpdateState.UnsupportedFirmwareVersion -> {
                        router.exit()
                        router.navigateTo(Screens.PlayMarketScreen)
                    }
                    is UpdateState.FirmwareDownloadingError -> downloadFirmwareAction.consumer.accept(
                        Unit
                    )
                    is UpdateState.FirmwareUpdateError -> startUpdateAction.consumer.accept(Unit)
                    is UpdateState.GlucometerOfflineError -> startUpdateAction.consumer.accept(Unit)
                    is UpdateState.Downloading -> {}
                    is UpdateState.Progress -> {}
                    is UpdateState.Updated -> {}
                    is UpdateState.Updating -> {}
                }
            }
            .untilDestroy()
    }

    private fun bindActions() {
        bindCheckUpdateAction()
        bindDeviceInfoAction()
        bindDownloadFirmwareAction()
        bindStartUpdateAction()
    }

    private fun bindStartUpdateAction() =
        startUpdateAction.observable
            .skipWhileInProgress(progressState.observable)
            .map(::createUpdateFirmwareUseCaseParams)
            .flatMapCompletable(::updateFirmware)
            .retry()
            .subscribe()
            .untilDestroy()

    private fun bindDownloadFirmwareAction() =
        downloadFirmwareAction.observable
            .skipWhileInProgress(progressState.observable)
            .map(::createDownloadFirmwareUseCaseParams)
            .flatMapSingle { params ->
                getFirmwareUseCase.execute(params)
                    .bindProgressExtended(progressState.consumer)
                    .doOnSubscribe {
                        setState(UpdateState.Downloading(resources, getDeviceVersion()))
                    }
                    .doOnSuccess(::handleFirmwareDownloaded)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

    private fun bindDeviceInfoAction() =
        getDeviceInfoAction.observable
            .skipWhileInProgress(progressState.observable)
            .map(::createGetDeviceInfoUseCaseParams)
            .flatMapSingle { params ->
                getLastGlucometerInfoUseCase.execute(params)
                    .bindProgressExtended(progressState.consumer)
                    .doOnSuccess(::handleDeviceInfo)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

    private fun bindCheckUpdateAction() =
        checkUpdatesAction.observable
            .skipWhileInProgress(progressState.observable)
            .flatMapSingle {
                getFirmwareInfoUseCase.execute()
                    .bindProgressExtended(progressState.consumer)
                    .doOnSubscribe {
                        setState(UpdateState.Progress(resources, getDeviceVersion()))
                    }
                    .doOnSuccess(::handleFirmwareInfo)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

    private fun setState(state: UpdateState) {
        delayedSetStateAction.consumer.accept(state)
    }

    private fun createGetDeviceInfoUseCaseParams(address: String): GetLastGlucometerInfoUseCase.Params =
        GetLastGlucometerInfoUseCase.Params(address)

    private fun handleDeviceInfo(info: GlucometerInfo) {
        deviceInfo.consumer.accept(info)
        setState(UpdateState.Progress(resources, info.softwareVersion?.toString()))
        checkUpdatesAction.consumer.accept(Unit)
    }

    private fun handleFirmwareInfo(firmware: Firmware) {
        firmwareState.consumer.accept(firmware)
        deviceInfo.valueOrNull?.let {
            val deviceVersionString = getDeviceVersion() ?: "0"
            if (it.isFirmwareNewer(firmware)) {
                setState(UpdateState.Found(resources, firmware.version, deviceVersionString))
            } else {
                setState(UpdateState.NotFound(resources, deviceVersionString))
            }
        }
    }

    private fun createDownloadFirmwareUseCaseParams(i: Unit): GetFirmwareUseCase.Params =
        GetFirmwareUseCase.Params(firmwareState.value)

    private fun handleFirmwareDownloaded(file: FirmwareFile) {
        firmwareFileState.consumer.accept(file)
        startUpdateAction.consumer.accept(Unit)
    }

    private fun createUpdateFirmwareUseCaseParams(i: Unit): UpdateDeviceFirmwareUseCase.Params =
        UpdateDeviceFirmwareUseCase.Params(
            address = deviceAddressState.valueOrNull ?: "",
            file = firmwareFileState.value
        )

    private fun handleFirmwareUpdated() {
        setState(UpdateState.Updated(resources, firmwareState.valueOrNull?.version))
    }

    private fun UpdateState?.hasUserInput(): Boolean = this?.button != null

    private fun getDeviceVersion(): String? =
        deviceInfo.valueOrNull?.softwareVersion?.toString()

    private fun <T> Single<T>.bindProgressExtended(progressConsumer: Consumer<Boolean>): Single<T> {
        return this
            .doOnSubscribe { progressConsumer.accept(true) }
            .doOnSuccess { progressConsumer.accept(false) }
            .doOnError { progressConsumer.accept(false) }
    }

    private fun Completable.bindProgressExtended(progressConsumer: Consumer<Boolean>): Completable {
        return this
            .doOnSubscribe { progressConsumer.accept(true) }
            .doOnComplete { progressConsumer.accept(false) }
            .doOnError { progressConsumer.accept(false) }
    }

    private fun updateFirmware(params: UpdateDeviceFirmwareUseCase.Params): Completable =
        updateDeviceFirmwareUseCase.execute(params)
            .bindProgressExtended(progressState.consumer)
            .doOnSubscribe {
                setState(UpdateState.Updating(resources, getDeviceVersion()))
            }
            .doOnComplete(::handleFirmwareUpdated)
            .onErrorResumeNext { error ->
                when (error) {
                    is BluetoothNotEnabledError ->
                        btControl.requestEnableBluetooth()
                            .flatMapCompletable {
                                if (it) updateFirmware(params)
                                else Completable.fromCallable {
                                    setState(UpdateState.GlucometerOfflineError(resources))
                                }
                            }
                    is LocationPermissionNotGrantedError ->
                        btControl.requestLocationPermissions()
                            .flatMapCompletable {
                                if (it) updateFirmware(params)
                                else Completable.fromCallable {
                                    setState(UpdateState.GlucometerOfflineError(resources))
                                }
                            }
                    is LocationNotEnabledError ->
                        btControl.requestEnableLocation()
                            .flatMapCompletable {
                                if (it) updateFirmware(params)
                                else Completable.fromCallable {
                                    setState(UpdateState.GlucometerOfflineError(resources))
                                }
                            }
                    else -> Completable.error(error)
                }
            }
            .doOnError(::handleError)

    companion object {
        private const val ZERO_DELAY = 0L
        private const val NEXT_STATE_DELAY = 1500L
    }
}
