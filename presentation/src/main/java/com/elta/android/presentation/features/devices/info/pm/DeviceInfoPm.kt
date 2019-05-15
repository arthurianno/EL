package com.elta.android.presentation.features.devices.info.pm

import com.elta.android.presentation.Dialogs
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.ui.dialog.DialogData
import com.elta.android.presentation.features.devices.info.ui.adapter.items.DeviceInfoItem
import me.dmdev.rxpm.widget.dialogControl
import javax.inject.Inject

class DeviceInfoPm @Inject constructor(
    services: ServiceFacade
) : BaseListPm(services) {

    val deleteDeviceDialogControl = dialogControl<DialogData, DialogResult>()

     val deleteDeviceAction = Action<Unit>()

    private val deleteDeviceDialogData: DialogData by lazy { Dialogs.DeleteDevice(resources) }


    override fun onCreate() {
        super.onCreate()

        deleteDeviceAction.observable
            .skipWhileInProgress()
            .switchMapMaybe {
                deleteDeviceDialogControl.showForResult(deleteDeviceDialogData)
            }
            .filter { it == DialogResult.POSITIVE }
//            .flatMapCompletable { params ->
//                deleteObserverUseCase.execute(params)
//                    .hideErrorContainer()
//                    .bindProgress()
//                    .doOnComplete(::handleDeletingSuccess)
//                    .doOnError(::handleError)
//            }
            .retry()
            .subscribe()
            .untilDestroy()

        // todo only for testing
        lifecycleObservable.filter { it == Lifecycle.CREATED }
            .map { arrayListOf(DeviceInfoItem("dmdmd", "sssm")) }
            .doOnNext(items.consumer)
            .subscribe()
            .untilDestroy()
    }

    enum class DialogResult {
        NEGATIVE, POSITIVE
    }
}