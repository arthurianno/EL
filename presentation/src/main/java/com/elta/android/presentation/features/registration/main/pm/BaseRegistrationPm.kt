package com.elta.android.presentation.features.registration.main.pm

import android.content.Context
import com.elta.android.domain.features.multiLangsConfig.interactor.GetScreenConfigFromCache
import com.elta.android.presentation.Screens
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEventType
import com.elta.android.presentation.core.pm.ScreenConfigurable
import com.elta.android.presentation.core.pm.ServiceFacade
import me.dmdev.rxpm.action
import me.dmdev.rxpm.command
import me.dmdev.rxpm.state

abstract class BaseRegistrationPm(
    services: ServiceFacade,
    getScreenConfigUseCase: GetScreenConfigFromCache,
    private val context: Context
) : BaseAuthPm(services), ScreenConfigurable {

    // Реализуем интерфейс ScreenConfigurable
    override val screenConfigKey = "registration-screens"
    override val getScreenConfigUseCase = getScreenConfigUseCase

    val privacyPolicyAcceptAction = action<Boolean>()
    val privacyPolicyClickAction = action<Unit>()
    val personalDataClickAction = action<Unit>()
    val backHandleAction = action<Unit>()
    val openPrivacyPolicyCommand = command<Unit>()
    val openPersonalDataCommand = command<Unit>()

    protected val privacyPolicyAcceptedState = state<Boolean>()

    override fun onCreate() {
        super.onCreate()

        // Загружаем конфигурацию через метод из BasePm
        loadScreenConfig(context)

        privacyPolicyClickAction.observable
            .trackEvent(AnalyticsEventType.TERMS_OF_USE)
            .subscribe(openPrivacyPolicyCommand.consumer)
            .untilDestroy()

        personalDataClickAction.observable
            .subscribe(openPersonalDataCommand.consumer)
            .untilDestroy()

        privacyPolicyAcceptAction.observable
            .subscribe(privacyPolicyAcceptedState.consumer)
            .untilDestroy()

        backHandleAction.observable
            .subscribe { router.navigateTo(Screens.GreetingFlow) }
            .untilDestroy()
    }
}