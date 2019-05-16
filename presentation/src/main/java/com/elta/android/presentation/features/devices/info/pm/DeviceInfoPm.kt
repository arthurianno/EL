package com.elta.android.presentation.features.devices.info.pm

import com.elta.android.domain.features.devices.interactor.DeleteGlucometerUseCase
import com.elta.android.domain.features.devices.interactor.GetGlucometerInfoUseCase
import com.elta.android.domain.features.devices.model.GlucometerInfo
import com.elta.android.presentation.Dialogs
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.ui.dialog.DialogData
import com.elta.android.presentation.features.devices.info.ui.builder.DeviceInfoItemsBuilder
import com.elta.android.presentation.messages.SnackBarMessageData
import me.dmdev.rxpm.widget.dialogControl
import javax.inject.Inject

class DeviceInfoPm @Inject constructor(
    private val getGlucometerInfoUseCase: GetGlucometerInfoUseCase,
    private val deleteGlucometerUseCase: DeleteGlucometerUseCase,
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

    private val deleteDeviceDialogData: DialogData by lazy { Dialogs.DeleteDevice(resources) }

    override fun onCreate() {
        super.onCreate()

        getDeviceInfoAction.observable
            .skipWhileInProgress()
            .map { addressState.value }
            .map(::createGetDeviceInfoParams)
            .flatMapSingle { params ->
                getGlucometerInfoUseCase.execute(params)
                    .hideErrorContainer()
                    .bindProgress()
                    .doOnSuccess(::handleSuccess)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        deleteDeviceAction.observable
            .switchMapMaybe {
                deleteDeviceDialogControl.showForResult(deleteDeviceDialogData)
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

        addressState.observable
            .map { resources.getString(R.string.profile_device_info_description, it) }
            .subscribe()
            .untilDestroy()

        checkUpdateAction.observable
            .doOnNext {
                showSnackBar(
                    SnackBarMessageData.SimpleTextMessage("Update firmware clicked...")
                )
            }
            .retry()
            .subscribe()
            .untilDestroy()
    }

    fun setDeviceData(name: String, address: String) {
        nameDeviceState.consumer.accept(name)
        addressState.consumer.accept(address)
        getDeviceInfoAction.consumer.accept(Unit)
    }

    private fun createDeleteDeviceParams(address: String) =
        DeleteGlucometerUseCase.Params(address)

    private fun createGetDeviceInfoParams(address: String) =
        GetGlucometerInfoUseCase.Params(address)

    private fun handleDeletingSuccess() {
        bus.event(Events.DeviceChanged)
        router.exit()
    }

    private fun handleSuccess(info: GlucometerInfo) {
        items.consumer.accept(itemsBuilder.buildItems(info))
    }

    enum class DialogResult {
        NEGATIVE, POSITIVE
    }
}