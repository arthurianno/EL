package com.elta.android.presentation.features.devices.info.pm

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
import com.elta.android.presentation.core.bus.clicks
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.ui.dialog.DialogData
import com.elta.android.presentation.core.ui.dialog.DialogResult
import com.elta.android.presentation.features.devices.info.ui.builder.DeviceInfoItemsBuilder
import io.reactivex.Observable
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
    val deleteDeviceAction = Action<Unit>()
    val checkUpdateAction = Action<Unit>()
    val nameDeviceState = State<String>()
    val descriptionAddressState = State<String>()

    private val addressState = State<String>()
    private val getDeviceInfoAction = Action<Unit>()

    private var glucometer: Glucometer? = null

    override fun onCreate() {
        super.onCreate()

        observeGetDeviceAction()
        observeDeleteDeviceAction()
        observeSetPrimaryDeviceClicks()

        addressState.observable
            .map { resources.getString(R.string.profile_device_info_description, it) }
            .subscribe(descriptionAddressState.consumer)
            .untilDestroy()

        checkUpdateAction.observable
            .skipWhileInProgress()
            .subscribe { router.navigateTo(Screens.UpdateFirmware(addressState.value)) }
            .untilDestroy()

        bus.events<Events.FirmwareUpdated>()
            .map { Unit }
            .subscribe(getDeviceInfoAction.consumer)
            .untilDestroy()
    }

    fun setDeviceData(name: String, address: String) {
        nameDeviceState.consumer.accept(name)
        addressState.consumer.accept(address)
        getDeviceInfoAction.consumer.accept(Unit)
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
        items.consumer.accept(itemsBuilder.buildItems(data.second, data.first.isPrimary))
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