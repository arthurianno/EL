package com.elta.android.presentation.features.profile.settings.dialogs.diabetes.pm

import com.elta.android.domain.features.user.model.Diabetes
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import javax.inject.Inject

class DiabetesSettingDialogPm @Inject constructor(
    services: ServiceFacade
) : BasePm(services) {

    val diabetesTypeSelectedAction = Action<Diabetes>()
    val diabetesState = State(Diabetes.values())
    val selectedDiabetesState = State<Diabetes>()
    val actionButtonEnabledCommand = State(false)

    override fun onCreate() {
        super.onCreate()
        diabetesTypeSelectedAction.observable
            .filter { it != selectedDiabetesState.valueOrNull }
            .doOnNext { actionButtonEnabledCommand.consumer.accept(true) }
            .subscribe(selectedDiabetesState.consumer)
            .untilDestroy()
    }
}