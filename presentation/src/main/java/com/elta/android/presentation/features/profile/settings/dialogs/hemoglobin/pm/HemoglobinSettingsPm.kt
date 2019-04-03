package com.elta.android.presentation.features.profile.settings.dialogs.hemoglobin.pm

import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.profile.settings.dialogs.base.pm.BaseSettingsDialogPm
import com.elta.android.presentation.utils.toEventDate
import java.util.Date
import javax.inject.Inject

class HemoglobinSettingsPm @Inject constructor(
    services: ServiceFacade
) : BaseSettingsDialogPm(services) {

    val dateState = State("")
    val dateSelectedAction = Action<Date>()

    val hemoglobinValueState = State("5,6")

    val minusAction = Action<Unit>()
    val plusAction = Action<Unit>()

    private val dateSelectedState = State(Date())

    override fun onCreate() {
        super.onCreate()

        dateSelectedAction.observable
            .subscribe(dateSelectedState.consumer)
            .untilDestroy()

        dateSelectedState.observable
            .doOnNext { dateState.consumer.accept(it.toEventDate(resources)) }
            .subscribe()
            .untilDestroy()
    }
}