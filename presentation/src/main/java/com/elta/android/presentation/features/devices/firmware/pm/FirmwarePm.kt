package com.elta.android.presentation.features.devices.firmware.pm

import com.elta.android.domain.features.devices.interactor.GetLastGlucometerInfoUseCase
import com.elta.android.domain.features.devices.interactor.isFirmwareNewer
import com.elta.android.domain.features.devices.model.GlucometerInfo
import com.elta.android.domain.features.firmware.interactor.GetFirmwareInfoUseCase
import com.elta.android.domain.features.firmware.model.Firmware
import com.elta.android.presentation.R
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.ui.dialog.DialogData
import com.elta.android.presentation.features.sync.control.bluetoothControl
import com.nullgr.core.resources.ResourceProvider
import io.reactivex.Observable
import me.dmdev.rxpm.widget.dialogControl
import javax.inject.Inject

class FirmwarePm @Inject constructor(
    private val getLastGlucometerInfoUseCase: GetLastGlucometerInfoUseCase,
    private val getFirmwareInfoUseCase: GetFirmwareInfoUseCase,
    services: ServiceFacade
) : BasePm(services) {

    val deviceAddressState = State<String>()
    val deviceInfo = State<GlucometerInfo>()
    val buttonAction = Action<Unit>()
    val updateState = State<UpdateState>(UpdateState.Progress(resources))
    val lowBatteryDialogControl = dialogControl<DialogData, Unit>()

    val btControl = bluetoothControl()

    private val getDeviceInfoAction = Action<String>()
    private val checkUpdatesAction = Action<Unit>()
    private val startUpdateAction = Action<Unit>()

    override fun onCreate() {
        super.onCreate()

        buttonAction.observable
            .filter { updateState.value is UpdateState.NotFound }
            .subscribe(checkUpdatesAction.consumer)
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

        Observable.merge(
            buttonAction.observable.filter { updateState.value is UpdateState.Found },
            btControl.bluetoothEnabledAction.observable,
            btControl.locationPermissionsGrantedAction.observable,
            btControl.locationEnabledAction.observable
        )
            .subscribe(startUpdateAction.consumer)
            .untilDestroy()

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
        deviceInfo.valueOrNull?.let {
            val deviceVersionString = deviceInfo.valueOrNull?.softwareVersion?.toString() ?: "0"
            if (it.isFirmwareNewer(firmware)) {
                updateState.consumer.accept(UpdateState.Found(resources, firmware.version, deviceVersionString))
            } else {
                updateState.consumer.accept(UpdateState.NotFound(resources, deviceVersionString))
            }
        }
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
    }
}