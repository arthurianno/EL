package com.elta.android.presentation.features.devices.firmware.pm

import android.os.Build
import com.elta.android.common.errors.BluetoothNotEnabledError
import com.elta.android.common.errors.FirmwareDownloadingError
import com.elta.android.common.errors.FirmwareUpdateError
import com.elta.android.common.errors.GlucometerLowBatteryLevelError
import com.elta.android.common.errors.GlucometerOfflineError
import com.elta.android.common.errors.LocationNotEnabledError
import com.elta.android.common.errors.LocationPermissionNotGrantedError
import com.elta.android.common.errors.NotFoundError
import com.elta.android.domain.features.devices.interactor.GetLastGlucometerInfoUseCase
import com.elta.android.domain.features.devices.interactor.UpdateDeviceFirmwareUseCase
import com.elta.android.domain.features.devices.model.GlucometerInfo
import com.elta.android.domain.features.firmware.interactor.DownloadFirmwareUseCase
import com.elta.android.domain.features.firmware.interactor.GetFirmwareInfoUseCase
import com.elta.android.domain.features.firmware.model.FirmwareFile
import com.elta.android.domain.features.firmware.model.FirmwareInfo
import com.elta.android.presentation.Dialogs
import com.elta.android.presentation.Events
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.ui.dialog.DialogData
import com.elta.android.presentation.core.ui.dialog.DialogResult
import com.elta.android.presentation.features.sync.control.bluetoothControl
import com.elta.android.presentation.features.sync.control.handlePermissionResult
import com.elta.android.presentation.features.sync.control.isBluetoothName
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.Consumer
import me.dmdev.rxpm.action
import me.dmdev.rxpm.skipWhileInProgress
import me.dmdev.rxpm.state
import me.dmdev.rxpm.widget.dialogControl
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Suppress("TooManyFunctions")
class FirmwarePm @Inject constructor(
    private val getLastGlucometerInfoUseCase: GetLastGlucometerInfoUseCase,
    private val downloadFirmwareUseCase: DownloadFirmwareUseCase,
    private val getFirmwareInfoUseCase: GetFirmwareInfoUseCase,
    private val updateDeviceFirmwareUseCase: UpdateDeviceFirmwareUseCase,
    services: ServiceFacade
) : BasePm(services) {

    val buttonAction = action<Unit>()
    val updateState = state<UpdateState>(UpdateState.Progress(resources))

    val btControl = bluetoothControl()

    private val getDeviceInfoAction = action<String>()
    private val checkUpdatesAction = action<Unit>()
    private val startUpdateAction = action<Unit>()
    private val downloadFirmwareAction = action<Unit>()

    private val deviceAddressState = state<String>()
    private val deviceInfo = state<GlucometerInfo>()
    private val firmwareInfoState = state<FirmwareInfo>()
    private val firmwareFileState = state<FirmwareFile>()

    private val delayedSetStateAction = action<UpdateState>()

    val settingsDialog = dialogControl<DialogData, DialogResult>()
    val settingsIsVisible = state(false)
    val openSettingsCloseAction = action<Unit>()

    private val settingsLocationDialogData: DialogData by lazy {
        Dialogs.SettingsLocationDialogData(resources)
    }
    private val settingsBluetoothDialogData: DialogData by lazy {
        Dialogs.SettingsBluetoothDialogData(resources)
    }

    override fun onCreate() {
        super.onCreate()

        bindStateBehavior()
        bindCheckUpdateAction()
        bindDeviceInfoAction()
        bindDownloadFirmwareAction()
        bindStartUpdateAction()
        bindBluetoothAndLocationAction()
        bindPermissionsAction()
    }

    override fun handleError(error: Throwable) {
        when (error) {
            is GlucometerLowBatteryLevelError -> setState(UpdateState.BatteryLowLevel(resources))
            is FirmwareDownloadingError -> setState(UpdateState.FirmwareDownloadingError(resources))
            is FirmwareUpdateError -> setState(UpdateState.FirmwareUpdateError(resources))
            is GlucometerOfflineError -> setState(UpdateState.GlucometerOfflineError(resources))
            is NotFoundError -> setState(UpdateState.NotFound(resources, getDeviceVersion()))
            is BluetoothNotEnabledError -> setState(UpdateState.FirmwareUpdateError(resources))
            else -> setState(UpdateState.NotFound(resources, getDeviceVersion()))
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
                    is UpdateState.Found -> downloadFirmwareAction.consumer.accept(Unit)
                    is UpdateState.BatteryLowLevel -> router.exit()
                    is UpdateState.FirmwareDownloadingError ->
                        downloadFirmwareAction.consumer.accept(Unit)

                    is UpdateState.FirmwareUpdateError ->
                        startUpdateAction.consumer.accept(Unit)

                    is UpdateState.GlucometerOfflineError ->
                        startUpdateAction.consumer.accept(Unit)

                    is UpdateState.Downloading -> {}
                    is UpdateState.Progress -> {}
                    is UpdateState.Updated -> {}
                    is UpdateState.Updating -> {}
                }
            }
            .untilDestroy()
    }

    private fun bindBluetoothAndLocationAction() {
        Observable.merge(
            btControl.bluetoothEnabledAction.observable,
            btControl.locationEnabledAction.observable
        )
            .subscribe(startUpdateAction.consumer)
            .untilDestroy()

        Observable.merge(
            btControl.bluetoothDeniedAction.observable,
            btControl.locationDeniedAction.observable
        )
            .subscribe {
                setState(UpdateState.GlucometerOfflineError(resources))
            }
            .untilDestroy()
    }

    private fun bindPermissionsAction() {
        Observable.merge(
            btControl.locationPermissionsGrantedAction.observable,
            btControl.bluetoothPermissionsGrantedAction.observable
        )
            .subscribe { permission ->
                val dialogData =
                    if (permission.name.isBluetoothName()) settingsBluetoothDialogData
                    else settingsLocationDialogData

                permission.handlePermissionResult(
                    onPermissionGranted = {
                        startUpdateAction.consumer.accept(Unit)
                    },
                    shouldShowPermissionRationale = {
                        showSettingDialog(dialogData)
                    },
                    onPermissionDenied = {
                        setState(UpdateState.GlucometerOfflineError(resources))
                    }
                )
            }
            .untilDestroy()

        openSettingsCloseAction.observable
            .subscribe { settingsIsVisible.accept(false) }
            .untilDestroy()
    }

    private fun bindStartUpdateAction() =
        startUpdateAction.observable
            .skipWhileInProgress(progressState.observable)
            .map(::createUpdateFirmwareUseCaseParams)
            .flatMap(::updateFirmware)
            .retry()
            .subscribe()
            .untilDestroy()

    private fun bindDownloadFirmwareAction() =
        downloadFirmwareAction.observable
            .skipWhileInProgress(progressState.observable)
            .map(::createDownloadFirmwareUseCaseParams)
            .flatMapSingle { params ->
                downloadFirmwareUseCase.execute(params)
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
                deviceInfo.valueOrNull?.let { glucometerInfo ->
                    getFirmwareInfoUseCase.execute(GetFirmwareInfoUseCase.Params(
                        modelId = getModel(),
                        currentVersion = getDeviceVersion(),
                        glucometerInfo = glucometerInfo
                    ))
                        .bindProgressExtended(progressState.consumer)
                        .doOnSubscribe { setState(UpdateState.Progress(resources, getDeviceVersion())) }
                        .doOnSuccess(::handleNewVersionModelFirmwareInfo)
                        .doOnError(::handleError)
                } ?: Single.error(NotFoundError("Device not found"))
            }
            .retry()
            .subscribe()
            .untilDestroy()

    private fun setState(state: UpdateState) {
        delayedSetStateAction.consumer.accept(state)
    }

    private fun showSettingDialog(dialogData: DialogData) {
        setState(UpdateState.NotFound(resources))
        settingsDialog.showForResult(dialogData)
            .map { it == DialogResult.POSITIVE }
            .subscribe(settingsIsVisible.consumer)
            .untilDestroy()
    }

    private fun createGetDeviceInfoUseCaseParams(address: String): GetLastGlucometerInfoUseCase.Params =
        GetLastGlucometerInfoUseCase.Params(address)

    private fun handleDeviceInfo(info: GlucometerInfo) {
        deviceInfo.consumer.accept(info)
        setState(UpdateState.Progress(resources, info.softwareVersion))
        checkUpdatesAction.consumer.accept(Unit)
    }

    private fun handleNewVersionModelFirmwareInfo(firmwareInfo: FirmwareInfo) {
        firmwareInfoState.consumer.accept(firmwareInfo)
        deviceInfo.valueOrNull?.let {
            setState(UpdateState.Found(resources, firmwareInfo.version, getDeviceVersion()))
        }
    }

    private fun createDownloadFirmwareUseCaseParams(i: Unit): DownloadFirmwareUseCase.Params =
        DownloadFirmwareUseCase.Params(firmwareInfo = firmwareInfoState.value)

    private fun handleFirmwareDownloaded(file: FirmwareFile) {
        firmwareFileState.consumer.accept(file)
        startUpdateAction.consumer.accept(Unit)
    }

    private fun createUpdateFirmwareUseCaseParams(i: Unit): UpdateDeviceFirmwareUseCase.Params =
        UpdateDeviceFirmwareUseCase.Params(
            address = deviceAddressState.valueOrNull.orEmpty(),
            file = firmwareFileState.value
        )

    private fun handleFirmwareUpdated() {
        setState(UpdateState.Updated(resources, firmwareInfoState.valueOrNull?.version))
    }

    private fun UpdateState?.hasUserInput(): Boolean = this?.button != null

    private fun getModel(): String =
        deviceInfo.valueOrNull?.glucometerSerialNumber.orEmpty()

    private fun getDeviceVersion(): String =
        deviceInfo.valueOrNull?.softwareVersion ?: "0"

    private fun <T> Single<T>.bindProgressExtended(progressConsumer: Consumer<Boolean>): Single<T> {
        return this
            .doOnSubscribe { progressConsumer.accept(true) }
            .doOnSuccess { progressConsumer.accept(false) }
            .doOnError { progressConsumer.accept(false) }
    }

    private fun <T> Observable<T>.bindProgressExtended(progressConsumer: Consumer<Boolean>): Observable<T> {
        return this
            .doOnSubscribe { progressConsumer.accept(true) }
            .doOnComplete { progressConsumer.accept(false) }
            .doOnError { progressConsumer.accept(false) }
    }

    private fun updateFirmware(params: UpdateDeviceFirmwareUseCase.Params): Observable<String> =
        updateDeviceFirmwareUseCase.execute(params)
            .bindProgressExtended(progressState.consumer)
            .doOnSubscribe {
                setState(UpdateState.Updating(resources, getDeviceVersion()))
            }
            .doOnComplete(::handleFirmwareUpdated)
            .doOnError { error ->
                when (error) {
                    is BluetoothNotEnabledError ->
                        btControl.requestEnableBluetoothCommand.consumer.accept(Unit)

                    is LocationPermissionNotGrantedError ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            btControl.requestBluetoothPermissionCommand.consumer.accept(Unit)
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            btControl.requestCombinedPermissionsCommand.consumer.accept(Unit)
                        } else {
                            btControl.requestLocationPermissionsCommand.consumer.accept(Unit)
                        }

                    is LocationNotEnabledError ->
                        btControl.requestEnableLocationCommand.consumer.accept(Unit)

                    else -> handleError(error)
                }
            }

    companion object {
        private const val ZERO_DELAY = 0L
        private const val NEXT_STATE_DELAY = 1500L
    }
}
