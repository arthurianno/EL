package com.elta.android.presentation.features.devices.firmware.pm

import com.elta.android.common.errors.BluetoothNotEnabledError
import com.elta.android.common.errors.GlucometerLowBatteryLevelError
import com.elta.android.common.errors.LocationNotEnabledError
import com.elta.android.common.errors.LocationPermissionNotGrantedError
import com.elta.android.common.utils.log
import com.elta.android.domain.features.devices.interactor.GetLastGlucometerInfoUseCase
import com.elta.android.domain.features.devices.interactor.UpdateDeviceFirmwareUseCase
import com.elta.android.domain.features.devices.interactor.isFirmwareNewer
import com.elta.android.domain.features.devices.model.GlucometerInfo
import com.elta.android.domain.features.firmware.interactor.DownloadFirmwareUseCase
import com.elta.android.domain.features.firmware.interactor.GetFirmwareInfoUseCase
import com.elta.android.domain.features.firmware.model.Firmware
import com.elta.android.domain.features.firmware.model.FirmwareFile
import com.elta.android.presentation.R
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.sync.control.bluetoothControl
import com.nullgr.core.resources.ResourceProvider
import io.reactivex.Completable
import io.reactivex.Observable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class FirmwarePm @Inject constructor(
    private val getLastGlucometerInfoUseCase: GetLastGlucometerInfoUseCase,
    private val getFirmwareInfoUseCase: GetFirmwareInfoUseCase,
    private val downloadFirmwareUseCase: DownloadFirmwareUseCase,
    private val updateDeviceFirmwareUseCase: UpdateDeviceFirmwareUseCase,
    services: ServiceFacade
) : BasePm(services) {

    val deviceAddressState = State<String>()
    val deviceInfo = State<GlucometerInfo>()
    val buttonAction = Action<Unit>()
    val updateState = State<UpdateState>(UpdateState.Progress(resources))

    val btControl = bluetoothControl()

    private val getDeviceInfoAction = Action<String>()
    private val checkUpdatesAction = Action<Unit>()
    private val startUpdateAction = Action<Unit>()
    private val downloadFirmwareAction = Action<Unit>()

    private val firmwareState = State<Firmware>()
    private val firmwareFileState = State<FirmwareFile>()

    override fun onCreate() {
        super.onCreate()

        buttonAction.observable
            .subscribe {
                when (updateState.value) {
                    is UpdateState.NotFound -> checkUpdatesAction.consumer.accept(Unit)
                    is UpdateState.Found -> downloadFirmwareAction.consumer.accept(Unit)
                    is UpdateState.BatteryLowLevel -> router.exit()
                }
            }
            .untilDestroy()

        checkUpdatesAction.observable
            .skipWhileInProgress()
            .flatMapSingle {
                getFirmwareInfoUseCase.execute()
                    .bindProgress()
                    .doOnSubscribe {
                        updateState.consumer.accept(UpdateState.Progress(resources, deviceInfo.valueOrNull?.softwareVersion?.toString()))
                    }
                    .doOnSuccess(::handleFirmwareInfo)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        getDeviceInfoAction.observable
            .map(::createGetDeviceInfoUseCaseParams)
            .flatMapSingle { params ->
                getLastGlucometerInfoUseCase.execute(params)
                    .doOnSuccess(::handleDeviceInfo)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        downloadFirmwareAction.observable
            .skipWhileInProgress()
            .map(::createDownloadFirmwareUseCaseParams)
            .flatMapSingle { params ->
                downloadFirmwareUseCase.execute(params)
                    .bindProgress()
                    .doOnSubscribe {
                        updateState.consumer.accept(UpdateState.Downloading(resources, deviceInfo.valueOrNull?.softwareVersion?.toString()))
                    }
                    .doOnSuccess(::handleFirmwareDownloaded)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        startUpdateAction.observable
            .log("FirmwarePm", "before")
            .log("FirmwarePm", "after")
            .map(::createUpdateFirmwareUseCaseParams)
            .flatMapCompletable { params ->
                updateDeviceFirmwareUseCase.execute(params)
                    .bindProgress()
                    .doOnSubscribe {
                        updateState.consumer.accept(UpdateState.Updating(resources, deviceInfo.valueOrNull?.softwareVersion?.toString()))
                    }
                    .doOnComplete(::handleFirmwareUpdated)
                    .doOnError(::handleError)
                    .andThen(
                        Completable.fromCallable {
                            router.exit()
                        }.delay(5, TimeUnit.SECONDS)
                    )
            }
            .retry()
            .subscribe()
            .untilDestroy()

        Observable.merge(
            btControl.bluetoothEnabledAction.observable,
            btControl.locationPermissionsGrantedAction.observable,
            btControl.locationEnabledAction.observable
        )
            .subscribe(startUpdateAction.consumer)
            .untilDestroy()
    }

    override fun handleError(error: Throwable) {
        when (error) {
            is BluetoothNotEnabledError -> btControl.requestEnableBluetoothCommand.consumer.accept(Unit)
            is LocationPermissionNotGrantedError -> btControl.requestLocationPermissionsCommand.consumer.accept(Unit)
            is LocationNotEnabledError -> btControl.requestEnableLocationCommand.consumer.accept(Unit)
            is GlucometerLowBatteryLevelError -> updateState.consumer.accept(UpdateState.BatteryLowLevel(resources, error.current))
            else -> super.handleError(error)
        }
    }

    fun setDeviceAddress(address: String) {
        deviceAddressState.consumer.accept(address)
        getDeviceInfoAction.consumer.accept(address)
    }

    private fun createGetDeviceInfoUseCaseParams(address: String): GetLastGlucometerInfoUseCase.Params =
        GetLastGlucometerInfoUseCase.Params(address)

    private fun handleDeviceInfo(info: GlucometerInfo) {
        deviceInfo.consumer.accept(info)
        updateState.consumer.accept(UpdateState.Progress(resources, info.softwareVersion?.toString()))
        checkUpdatesAction.consumer.accept(Unit)
    }

    private fun handleFirmwareInfo(firmware: Firmware) {
        firmwareState.consumer.accept(firmware)
        deviceInfo.valueOrNull?.let {
            val deviceVersionString = deviceInfo.valueOrNull?.softwareVersion?.toString() ?: "0"
            if (it.isFirmwareNewer(firmware)) {
                updateState.consumer.accept(UpdateState.Found(resources, firmware.version, deviceVersionString))
            } else {
                updateState.consumer.accept(UpdateState.NotFound(resources, deviceVersionString))
            }
        }
    }

    private fun createDownloadFirmwareUseCaseParams(i: Unit): DownloadFirmwareUseCase.Params =
        DownloadFirmwareUseCase.Params(firmwareState.value)

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
        updateState.consumer.accept(UpdateState.Updated(resources, firmwareState.valueOrNull?.version))
    }

    sealed class UpdateState {

        abstract val title: String
        abstract val version: String?
        abstract val hint: String?
        abstract val button: String?

        data class Progress(
            val resources: ResourceProvider,
            val currentVersion: String? = null,
            override val title: String = resources.getString(R.string.firmware_title_checking_updates),
            override val version: String? = currentVersion?.let { resources.getString(R.string.firmware_version_current, it) },
            override val hint: String? = null,
            override val button: String? = null
        ) : UpdateState()

        data class NotFound(
            val resources: ResourceProvider,
            val currentVersion: String? = null,
            override val title: String = resources.getString(R.string.firmware_title_updates_not_found),
            override val version: String? = currentVersion?.let { resources.getString(R.string.firmware_version_current, it) },
            override val hint: String? = null,
            override val button: String? = resources.getString(R.string.firmware_button_check_updates)
        ) : UpdateState()

        data class Found(
            val resources: ResourceProvider,
            val newVersion: String,
            val currentVersion: String? = null,
            override val title: String = resources.getString(R.string.firmware_title_updates_found, newVersion),
            override val version: String? = currentVersion?.let { resources.getString(R.string.firmware_version_current, it) },
            override val hint: String? = resources.getString(R.string.firmware_updates_hint),
            override val button: String? = resources.getString(R.string.firmware_button_update)
        ) : UpdateState()

        data class Downloading(
            val resources: ResourceProvider,
            val currentVersion: String? = null,
            override val title: String = resources.getString(R.string.firmware_title_downloading),
            override val version: String? = currentVersion?.let { resources.getString(R.string.firmware_version_current, it) },
            override val hint: String? = null,
            override val button: String? = null
        ) : UpdateState()

        data class Updating(
            val resources: ResourceProvider,
            val currentVersion: String? = null,
            override val title: String = resources.getString(R.string.firmware_title_updating),
            override val version: String? = currentVersion?.let { resources.getString(R.string.firmware_version_current, it) },
            override val hint: String? = null,
            override val button: String? = null
        ) : UpdateState()

        data class BatteryLowLevel(
            val resources: ResourceProvider,
            val currentLevel: Int,
            override val title: String = resources.getString(R.string.firmware_title_low_level, currentLevel),
            override val version: String? = resources.getString(R.string.firmware_description_low_level),
            override val hint: String? = null,
            override val button: String? = resources.getString(R.string.firmware_button_close)
        ) : UpdateState()

        data class Updated(
            val resources: ResourceProvider,
            val newVersion: String? = null,
            override val title: String = resources.getString(R.string.firmware_title_updated),
            override val version: String? = newVersion?.let { resources.getString(R.string.firmware_version_new, it) },
            override val hint: String? = null,
            override val button: String? = null
        ) : UpdateState()
    }
}