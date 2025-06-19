package com.elta.android.presentation.features.registration.flow.pm

import com.elta.android.domain.features.remoteconfig.interactor.GetFeatureConfigUseCase
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.pm.BaseFlowPm
import com.elta.android.presentation.core.pm.ServiceFacade
import javax.inject.Inject

@Deprecated("Файл служит для того чтобы перенаправить на следующий экран и ничего больше не делает")
class RegistrationFlowPm @Inject constructor(
    private val getFeatureConfigUseCase: GetFeatureConfigUseCase,
    services: ServiceFacade
) : BaseFlowPm(services) {

    override fun navigateToLaunchScreen() {
        val isNewRecoveryEnable = getFeatureConfigUseCase.invoke().recoveryAccount
        val screen = if (isNewRecoveryEnable) Screens.RegistrationMain
        else Screens.RegistrationMainVariantA
        router.newRootScreen(screen)
    }
}
