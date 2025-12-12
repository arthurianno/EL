package com.elta.android.presentation.features.sync.connect.viewmodel

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.viewModelScope
import coil.Coil.imageLoader
import coil.ImageLoader
import coil.request.ImageRequest
import com.elta.android.domain.features.multiLangsConfig.interactor.GetAllScreensUseCase
import com.elta.android.domain.features.multiLangsConfig.interactor.GetScreenConfigFromCache
import com.elta.android.domain.features.multiLangsConfig.model.Resource
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
import javax.inject.Inject

class ConnectStartViewModel @Inject constructor(
    private val appMetric: AppMetricTracker,
    private val getFeatureConfigUseCase: GetFeatureConfigUseCase,
    private val getScreenFromCacheUseCase: GetScreenConfigFromCache,
    private val context: Context
) : BaseViewModel<ConnectStartViewState>() {

    override fun createInitState() = ConnectStartViewState(
        isOnBoarding = true
    )

    internal val appTopBar = BaseAppTopBarWidgetModel()
    internal val downButton = DownButtonWidgetModel()

    init {
        // Устанавливаем дефолтный текст кнопки
        downButton.setText(context.getString(R.string.sync_start_action_button))

        viewModelScope.launch {
            when (val result = getScreenFromCacheUseCase("connect-start-onboarding")) {
                is Resource.Success -> {
                    val screen = result.data
                    Log.e("CONNECTSTART","${state.value}")
                    // Обновляете state с данными экрана
                    reduceState {
                        state.value.copy(
                            screenConfig = screen
                        )
                    }
                }
                is Resource.Error -> {
                    Log.e("ConnectStartViewModel", "Error fetching screen: ${result.message}")
                }
                is Resource.Loading -> {
                    Log.e("ConnectStartViewModel", "Loading screen config...")
                }
            }
        }
    }

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