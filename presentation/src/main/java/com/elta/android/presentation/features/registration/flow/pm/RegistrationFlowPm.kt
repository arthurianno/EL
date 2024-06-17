package com.elta.android.presentation.features.registration.flow.pm

import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.pm.BaseFlowPm
import com.elta.android.presentation.core.pm.ServiceFacade
import javax.inject.Inject

@Deprecated("Файл служит для того чтобы перенаправить на следующий экран и ничего больше не делает")
class RegistrationFlowPm @Inject constructor(
    services: ServiceFacade
) : BaseFlowPm(services) {

    override fun navigateToLaunchScreen() {
        router.newRootScreen(Screens.RegistrationMain)
    }
}
