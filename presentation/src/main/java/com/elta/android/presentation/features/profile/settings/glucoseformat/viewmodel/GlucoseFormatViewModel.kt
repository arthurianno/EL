package com.elta.android.presentation.features.profile.settings.glucoseformat.viewmodel

import com.elta.android.domain.features.user.interactor.GetUpdatedProfileUseCase
import com.elta.android.domain.features.user.interactor.UpdateProfileUseCase
import com.elta.android.domain.features.user.model.GlucoseFormat
import com.elta.android.domain.features.user.model.Profile
import com.elta.android.presentation.Events
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBarWidgetModel
import com.elta.android.presentation.core.compose.widgets.buttons.DownButtonClick
import com.elta.android.presentation.core.compose.widgets.buttons.DownButtonWidgetModel
import com.elta.android.presentation.features.profile.settings.glucoseformat.model.GlucoseFormatAction
import com.elta.android.presentation.features.profile.settings.glucoseformat.model.GlucoseFormatViewState
import com.nullgr.core.rx.RxBus
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.rx2.asFlow
import javax.inject.Inject

class GlucoseFormatViewModel @Inject constructor(
    private val getProfile: GetUpdatedProfileUseCase,
    private val updateProfile: UpdateProfileUseCase,
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
                    reduceState { state.value.copy(
                        profile = it,
                        initGlucoseFormat = it.glucoseFormat
                    ) }
                }
        }
    }

    val appTopBar: BaseAppTopBarWidgetModel = BaseAppTopBarWidgetModel()
    val downButton: DownButtonWidgetModel = DownButtonWidgetModel().apply { setEnableState(false) }

    override val widgets = listOf(
        appTopBar,
        downButton
    ).actionObserve()

    override fun handleUserAction(action: Action) {
        when(action) {
            is DownButtonClick -> {
                updateProfile.execute(UpdateProfileUseCase.Params(state.value.profile))
                    .subscribe(
                        {
                            bus.event(Events.EventsChanged(false))
                            backClick()
                        },
                        { handleError(it) }
                    )
            }
            is AppAction.BackPressure -> backClick()
        }
    }

    override fun reduceStateByAction(
        currentState: GlucoseFormatViewState,
        action: Action
    ): GlucoseFormatViewState = when (action) {
        is GlucoseFormatAction.SelectFormat -> {
            downButton.setEnableState(state.value.initGlucoseFormat != action.format)
            val newProfile = state.value.profile.copy(glucoseFormat = action.format)
            currentState.copy(profile = newProfile)
        }
        else -> currentState
    }
}
