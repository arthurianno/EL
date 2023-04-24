package com.elta.android.presentation.features.devices.info.pm

import com.elta.android.common.errors.BluetoothNotEnabledError
import com.elta.android.domain.features.devices.interactor.DeleteGlucometerUseCase
import com.elta.android.domain.features.devices.interactor.GetLastGlucometerAndInfoUseCase
import com.elta.android.domain.features.devices.interactor.SetPrimaryGlucometerUseCase
import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.devices.model.GlucometerInfo
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.Dialogs
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.Screens
import com.elta.android.presentation.analytics.model.AnalyticsEventType
import com.elta.android.presentation.core.bus.clicks
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.ui.dialog.DialogData
import com.elta.android.presentation.core.ui.dialog.DialogResult
import com.elta.android.presentation.features.devices.info.ui.builder.DeviceInfoItemsBuilder
import io.reactivex.Observable
import me.dmdev.rxpm.action
import me.dmdev.rxpm.command
import me.dmdev.rxpm.state
import me.dmdev.rxpm.widget.dialogControl
import javax.inject.Inject

class DeviceInfoPm @Inject constructor(
    private val getLastGlucometerAndInfoUseCase: GetLastGlucometerAndInfoUseCase,
    private val deleteGlucometerUseCase: DeleteGlucometerUseCase,
    private val setPrimaryGlucometerUseCase: SetPrimaryGlucometerUseCase,
    private val itemsBuilder: DeviceInfoItemsBuilder,
    services: ServiceFacade
) : BaseListPm(services) {

    val deleteDeviceDialogControl = dialogControl<DialogData, DialogResult>()
    val deleteDeviceAction = action<Unit>()
    val checkUpdateAction = action<Unit>()
    val nameDeviceState = state<String>()
    val descriptionAddressState = state<String>()

    private val addressState = state<String>()
    private val getDeviceInfoAction = action<Unit>()

    private var glucometer: Glucometer? = null
    val requestEnableBluetoothCommand = command<Unit>(bufferSize = 1)
    val bluetoothEnabledAction = action<Unit>()

    override fun onCreate() {
        super.onCreate()

        observeGetDeviceAction()
        observeDeleteDeviceAction()
        observeSetPrimaryDeviceClicks()
        observeBluetoothEnable()

        checkUpdateAction.observable
            .skipWhileInProgress()
            .subscribe { router.navigateTo(Screens.UpdateFirmware(addressState.value)) }
            .untilDestroy()

        observeEvents()
    }

    fun setDeviceData(name: String, address: String) {
        nameDeviceState.consumer.accept(name)
        addressState.consumer.accept(address)
        getDeviceInfoAction.consumer.accept(Unit)
    }

    private fun observeBluetoothEnable() {
        bluetoothEnabledAction.observable
            .doOnError(::handleError)
            .trackEvent(AnalyticsEventType.TURN_ON_SEARCH)
            .subscribe {
                glucometer?.address?.let { router.navigateTo(Screens.DeviceSearch(it)) }
            }
            .untilDestroy()
    }

    private fun observeEvents() {
        bus.events<Events.FirmwareUpdated>()
            .map { Unit }
            .subscribe(getDeviceInfoAction.consumer)
            .untilDestroy()

        bus.clicks<Clicks.OpenBlueToothScreen>()
            .map { Unit }
            .subscribe { router.navigateTo(Screens.BluetoothScreen) }
            .untilDestroy()

        bus.clicks<Clicks.DeviceSearchItemClicked>()
            .map { Unit }
            .subscribe {
                requestEnableBluetoothCommand.consumer.accept(Unit)
            }
            .untilDestroy()
    }

    private fun observeSetPrimaryDeviceClicks() =
        bus.clicks<Clicks.PrimaryDeviceItemClicked>()
            .map { addressState.value }
            .map(::createSetPrimaryDeviceParams)
            .flatMap {
                setPrimaryGlucometerUseCase.execute(it)
                    .hideErrorContainer()
                    .bindProgress()
                    .andThen(loadGlucometerInfo(Unit))
                    .doOnNext { bus.event(Events.DeviceChanged) }
                    .doOnError(::handleError)
            }
            .subscribe()
            .untilDestroy()

    private fun observeDeleteDeviceAction() =
        deleteDeviceAction.observable
            .skipWhileInProgress()
            .switchMapMaybe {
                glucometer?.let {
                    deleteDeviceDialogControl.showForResult(
                        Dialogs.DeleteDevice(resources, it.isPrimary)
                    )
                }
            }
            .filter { it == DialogResult.POSITIVE }
            .map { addressState.value }
            .map(::createDeleteDeviceParams)
            .flatMapCompletable { params ->
                deleteGlucometerUseCase.execute(params)
                    .hideErrorContainer()
                    .bindProgress()
                    .doOnComplete(::handleDeletingSuccess)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

    private fun observeGetDeviceAction() =
        getDeviceInfoAction.observable
            .skipWhileInProgress()
            .flatMap(::loadGlucometerInfo)
            .retry()
            .subscribe()
            .untilDestroy()

    private fun createDeleteDeviceParams(address: String) =
        DeleteGlucometerUseCase.Params(address)

    private fun createGetDeviceInfoParams(address: String) =
        GetLastGlucometerAndInfoUseCase.Params(address)

    private fun createSetPrimaryDeviceParams(address: String) =
        SetPrimaryGlucometerUseCase.Params(address)

    private fun handleDeletingSuccess() {
        bus.event(Events.DeviceChanged)
        router.exit()
    }

    private fun handleSuccess(data: Pair<Glucometer, GlucometerInfo>) {
        glucometer = data.first
        descriptionAddressState.consumer.accept(
            resources.getString(
                R.string.profile_device_info_description,
                data.second.glucometerSerialNumber.orEmpty()
            )
        )
        items.consumer.accept(itemsBuilder.buildItems(data.second, data.first.isPrimary))
    }

    override fun handleError(error: Throwable) {
        if (error == BluetoothNotEnabledError) requestEnableBluetoothCommand.consumer.accept(Unit)
        super.handleError(error)
    }

    private fun loadGlucometerInfo(i: Unit): Observable<Pair<Glucometer, GlucometerInfo>> =
        Observable.just(addressState.value)
            .map(::createGetDeviceInfoParams)
            .flatMap {
                getLastGlucometerAndInfoUseCase.execute(it)
                    .hideErrorContainer()
                    .bindProgress()
                    .doOnNext(::handleSuccess)
                    .doOnError(::handleError)
            }
}
