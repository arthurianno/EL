package com.elta.android.presentation.features.sync.pin.pm

import com.elta.android.domain.features.devices.interactor.isPinValid
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import me.dmdev.rxpm.widget.inputControl
import javax.inject.Inject

class PinDialogPm @Inject constructor(
    services: ServiceFacade
) : BasePm(services) {

    val actionButtonEnabledState = State(false)
    val mainAction = Action<Unit>()
    val closeDialogCommand = Command<Unit>(bufferSize = 1)
    val pinInputControl = inputControl()
    val deviceNameState = State("")

    override fun onCreate() {
        super.onCreate()

        pinInputControl.textChanges.observable
            .map(::isPinValid)
            .subscribe(actionButtonEnabledState.consumer)
            .untilDestroy()

        mainAction.observable
            .map { pinInputControl.text.valueOrNull ?: "" }
            .subscribe { pin ->
                closeDialogCommand.consumer.accept(Unit)
                bus.event(Events.PinCodeEntered(pin))
            }
            .untilDestroy()
    }

    fun setDeviceName(name: String) {
        deviceNameState.consumer.accept(resources.getString(R.string.sync_state_pin_dialog_device_pattern, name))
    }
}