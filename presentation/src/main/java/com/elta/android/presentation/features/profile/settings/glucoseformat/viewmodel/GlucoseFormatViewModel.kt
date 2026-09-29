package com.elta.android.presentation.features.profile.settings.glucoseformat.viewmodel

import android.os.Bundle
import com.elta.android.domain.features.user.interactor.GetUpdatedProfileUseCase
import com.elta.android.domain.features.user.interactor.UpdateProfileUseCase
import com.elta.android.domain.features.user.model.GlucoseFormat
import com.elta.android.domain.features.user.model.Profile
import com.elta.android.presentation.Events
import com.elta.android.presentation.Screens
import com.elta.android.presentation.analytic.core.appmetric.AppMetricTracker
import com.elta.android.presentation.analytic.getMetricName
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBarWidgetModel
import com.elta.android.presentation.core.compose.widgets.buttons.DownButtonClick
import com.elta.android.presentation.core.compose.widgets.buttons.DownButtonWidgetModel
import com.elta.android.presentation.features.profile.settings.glucoseformat.model.GlucoseFormatAction
import com.elta.android.presentation.features.profile.settings.glucoseformat.model.GlucoseFormatViewState
import com.elta.android.presentation.features.profile.settings.glucoseformat.DMC_FORMAT_ARGUMENT_NAME
import com.elta.android.presentation.features.profile.settings.glucoseformat.DMC_FORMAT_ON_BOARDING_ARGUMENT_NAME
import com.elta.android.presentation.features.profile.settings.glucoseformat.DMC_FORMAT_VARIANT_A_ARGUMENT_NAME
import com.nullgr.core.rx.RxBus
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.rx2.asFlow
import kotlinx.coroutines.rx2.await
import javax.inject.Inject

class GlucoseFormatViewModel @Inject constructor(
    private val getProfile: GetUpdatedProfileUseCase,
    private val updateProfile: UpdateProfileUseCase,
    private val appMetric: AppMetricTracker,
    private val bus: RxBus
) : BaseViewModel<GlucoseFormatViewState>() {
    override fun createInitState(): GlucoseFormatViewState =
        GlucoseFormatViewState(
            profile = Profile(glucoseFormat = GlucoseFormat.CAPILLARY),
            initGlucoseFormat = GlucoseFormat.CAPILLARY
        )

    init {
        launch {
            getProfile.execute()
                .toObservable()
                .asFlow()
                .catch { handleError(it) }
                .collectLatest {
                    reduceState {
                        val current = state.value
                        state.value.copy(
                            profile = if (current.selectionMade) {
                                it.copy(glucoseFormat = current.profile.glucoseFormat)
                            } else it,
                            initGlucoseFormat = it.glucoseFormat,
                            isProfileLoaded = true
                        )
                    }
                }
        }
    }

    val appTopBar: BaseAppTopBarWidgetModel = BaseAppTopBarWidgetModel()
    val downButton: DownButtonWidgetModel = DownButtonWidgetModel().apply { setEnableState(false) }

    override val widgets = listOf(
        appTopBar,
        downButton
    ).actionObserve()

    override fun handleFragmentArguments(arguments: Bundle) {
        reduceState {
            state.value.copy(
                isFromDmc = arguments.getBoolean(DMC_FORMAT_ARGUMENT_NAME),
                isOnBoarding = arguments.getBoolean(DMC_FORMAT_ON_BOARDING_ARGUMENT_NAME),
                isVariantA = arguments.getBoolean(DMC_FORMAT_VARIANT_A_ARGUMENT_NAME)
            )
        }
    }

    override fun handleUserAction(action: Action) {
        when (action) {
            is DownButtonClick, is GlucoseFormatAction.Save -> saveFormat()
            is AppAction.BackPressure -> backClick()
        }
    }

    private fun saveFormat() {
        val current = state.value
        if (current.isSaving || !current.isProfileLoaded || (current.isFromDmc && !current.selectionMade)) return
        reduceState { state.value.copy(isSaving = true) }
        appMetric.trackEvent(current.profile.glucoseFormat.getMetricName())

        launch {
            runCatching {
                updateProfile.execute(UpdateProfileUseCase.Params(state.value.profile)).await()
                bus.event(Events.EventsChanged(false))
                if (state.value.isFromDmc) {
                    if (state.value.isOnBoarding) {
                        router.newRootScreen(if (state.value.isVariantA) Screens.HomeFlowVariantA else Screens.HomeFlow)
                    } else {
                        router.backTo(Screens.Devices)
                    }
                } else {
                    backClick()
                }
            }.onFailure {
                reduceState { state.value.copy(isSaving = false) }
                handleError(it)
            }
        }
    }

    override fun reduceStateByAction(
        currentState: GlucoseFormatViewState,
        action: Action
    ): GlucoseFormatViewState = when (action) {
        is GlucoseFormatAction.SelectFormat -> {
            downButton.setEnableState(currentState.initGlucoseFormat != action.format)
            val newProfile = currentState.profile.copy(glucoseFormat = action.format)
            currentState.copy(profile = newProfile, selectionMade = true)
        }

        else -> currentState
    }
}
