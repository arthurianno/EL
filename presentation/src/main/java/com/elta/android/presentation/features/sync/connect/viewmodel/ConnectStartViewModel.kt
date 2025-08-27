package com.elta.android.presentation.features.sync.connect.viewmodel

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.viewModelScope
import com.elta.android.domain.features.multiLang.usecases.GetScreenConfigUseCase
import com.elta.android.domain.features.remoteconfig.interactor.GetFeatureConfigUseCase
import com.elta.android.presentation.R
import com.elta.android.presentation.Screens
import com.elta.android.presentation.analytic.core.appmetric.AppMetricTracker
import com.elta.android.presentation.analytic.model.appmetric.AppMetricEvent
import com.elta.android.presentation.analytic.model.appmetric.params.ConnectingPathParam
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBarWidgetModel
import com.elta.android.presentation.core.compose.widgets.buttons.DownButtonClick
import com.elta.android.presentation.core.compose.widgets.buttons.DownButtonWidgetModel
import com.elta.android.presentation.features.sync.connect.IS_ON_BOARDING_ARGUMENT_NAME
import com.elta.android.presentation.features.sync.connect.model.ConnectAction
import com.elta.android.presentation.features.sync.connect.model.ConnectStartViewState
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ConnectStartViewModel @Inject constructor(
    private val appMetric: AppMetricTracker,
    private val getFeatureConfigUseCase: GetFeatureConfigUseCase,
    private val getScreenConfigUseCase: GetScreenConfigUseCase,
    private val context: Context
) : BaseViewModel<ConnectStartViewState>() {

    override fun createInitState() = ConnectStartViewState(
        isOnBoarding = true,
        screenConfig = null
    )

    internal val appTopBar = BaseAppTopBarWidgetModel()
    internal val downButton = DownButtonWidgetModel()

    override val widgets = listOf(
        appTopBar,
        downButton
    ).actionObserve()

    override fun handleFragmentArguments(arguments: Bundle) {
        val isOnboarding = arguments.getBoolean(IS_ON_BOARDING_ARGUMENT_NAME)

        val eventParam = if (isOnboarding) ConnectingPathParam.ONBOARDING
        else ConnectingPathParam.SYNCHRONIZATION
        val eventName = AppMetricEvent.DeviceConnectingClick(eventParam)
        appMetric.trackEvent(eventName)

        reduceState {
            state.value.copy(isOnBoarding = isOnboarding)
        }

        // Запрашиваем конфигурацию для экрана и устанавливаем текст кнопки
        viewModelScope.launch {
            try {
                val config = getScreenConfigUseCase("connect_start")
                reduceState {
                    state.value.copy(screenConfig = config)
                }
                // Устанавливаем текст кнопки из конфигурации
                val buttonText = config?.description?.getTranslation("kk", defaultLang = "ru")
                Timber.d("Button text from config: $buttonText") // Логируем для отладки
                downButton.setText(buttonText ?: context.getString(R.string.sync_state_pin_dialog_button))
            } catch (e: Exception) {
                Timber.e(e, "Failed to load screen config for connect_start")
                downButton.setText(context.getString(R.string.sync_state_pin_dialog_button))
            }
        }
    }

    override fun handleUserAction(action: Action) {
        when (action) {
            is DownButtonClick -> router.navigateTo(Screens.ConnectTypeScreen(state.value.isOnBoarding))
            is ConnectAction.SkipNextStep -> {
                val improvedEnablingLocation = getFeatureConfigUseCase.invoke().improvedEnablingLocation
                val screen = if (improvedEnablingLocation) Screens.HomeFlow
                else Screens.HomeFlowVariantA
                router.newRootScreen(screen)
            }
        }
        super.handleUserAction(action)
    }
}