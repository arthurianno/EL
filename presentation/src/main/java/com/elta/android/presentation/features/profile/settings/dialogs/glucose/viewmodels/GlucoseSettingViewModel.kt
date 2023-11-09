package com.elta.android.presentation.features.profile.settings.dialogs.glucose.viewmodels

import androidx.compose.ui.text.input.TextFieldValue
import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings
import com.elta.android.domain.features.user.interactor.GetProfileUseCase
import com.elta.android.domain.features.user.model.Profile
import com.elta.android.presentation.Events
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBarWidgetModel
import com.elta.android.presentation.core.compose.widgets.buttons.DownButtonClick
import com.elta.android.presentation.core.compose.widgets.buttons.DownButtonWidgetModel
import com.elta.android.presentation.core.compose.widgets.dialogs.BaseDialogWidgetModel
import com.elta.android.presentation.core.compose.widgets.textfields.FocusChanged
import com.elta.android.presentation.core.compose.widgets.textfields.IconOutlinedTextFieldWidgetModel
import com.elta.android.presentation.features.profile.settings.dialogs.glucose.model.GlucoseLevel
import com.elta.android.presentation.features.profile.settings.dialogs.glucose.model.GlucoseRange
import com.elta.android.presentation.features.profile.settings.dialogs.glucose.model.GlucoseRangeError
import com.elta.android.presentation.features.profile.settings.dialogs.glucose.model.GlucoseSettingsState
import com.elta.android.presentation.features.profile.settings.dialogs.glucose.model.toDoubleFormat
import com.elta.android.presentation.features.profile.settings.dialogs.glucose.model.toGlucoseRange
import com.elta.android.presentation.utils.GLUCOSE_COUNT_INTEGER_PART
import com.elta.android.presentation.utils.GLUCOSE_INTEGER_LENGTH_REGEX
import com.elta.android.presentation.utils.GLUCOSE_VALUE_REGEX
import com.elta.android.presentation.utils.createTextFilterForDoubleValue
import com.nullgr.core.rx.RxBus
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

private const val DEFAULT_GLUCOSE_START = "3,9"
private const val DEFAULT_GLUCOSE_END = "10,0"
private val defaultGlucoseLevel = GlucoseLevel(
    beforeMeal = GlucoseRange(DEFAULT_GLUCOSE_START, DEFAULT_GLUCOSE_END),
    afterMeal = GlucoseRange(DEFAULT_GLUCOSE_START, DEFAULT_GLUCOSE_END)
)

class GlucoseSettingViewModel @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val bus: RxBus
) : BaseViewModel<GlucoseSettingsState>() {
    override fun createInitState(): GlucoseSettingsState = GlucoseSettingsState(
        currentGlucoseLevel = defaultGlucoseLevel,
        startGlucoseLevel = defaultGlucoseLevel,
        errorTypeBeforeMeal = GlucoseRangeError.NONE,
        errorTypeAfterMeal = GlucoseRangeError.NONE,
        profile = null,
        isLoading = true
    )

    val minBeforeMeal = IconOutlinedTextFieldWidgetModel()
    val maxBeforeMeal = IconOutlinedTextFieldWidgetModel()
    val minAfterMeal = IconOutlinedTextFieldWidgetModel()
    val maxAfterMeal = IconOutlinedTextFieldWidgetModel()

    val appTopBar = BaseAppTopBarWidgetModel()
    val downButton = DownButtonWidgetModel()

    val warningExitDialog = BaseDialogWidgetModel<Nothing>(positiveOnCLick = { router.exit() })

    override val widgets = listOf(
        appTopBar,
        downButton,
        minBeforeMeal,
        maxBeforeMeal,
        minAfterMeal,
        maxAfterMeal
    ).actionObserve()

    init {
        initTextFilter()
        initProfileValue()
        observeGlucoseRange()
    }

    override fun handleUserAction(action: Action) {
        when (action) {
            DownButtonClick -> saveRange()
            FocusChanged -> checkRange()
        }
        super.handleUserAction(action)
    }

    override fun backClick() {
        if (isRangeChanged(state.value.currentGlucoseLevel, state.value.startGlucoseLevel)) {
            warningExitDialog.dialogOpen()
        } else {
            super.backClick()
        }
    }

    private fun initTextFilter() {
        val filter: (TextFieldValue) -> TextFieldValue? = createTextFilterForDoubleValue(
            GLUCOSE_INTEGER_LENGTH_REGEX,
            GLUCOSE_VALUE_REGEX,
            GLUCOSE_COUNT_INTEGER_PART
        )


        minBeforeMeal.setFilter(filter)
        maxBeforeMeal.setFilter(filter)
        minAfterMeal.setFilter(filter)
        maxAfterMeal.setFilter(filter)
    }

    private fun observeGlucoseRange() {
        launch {
            combine(
                minBeforeMeal.state,
                maxBeforeMeal.state,
                minAfterMeal.state,
                maxAfterMeal.state
            ) { minBeforeMeal, maxBeforeMeal, minAfterMeal, maxAfterMeal ->
                GlucoseLevel(
                    beforeMeal = GlucoseRange(
                        minBeforeMeal.textField.text,
                        maxBeforeMeal.textField.text
                    ),
                    afterMeal = GlucoseRange(
                        minAfterMeal.textField.text,
                        maxAfterMeal.textField.text
                    )
                )
            }
                .filter { glucoseLevel -> glucoseLevel.isNotEmpty() }
                .collectLatest { glucoseLevel ->
                    reduceState { state.value.copy(currentGlucoseLevel = glucoseLevel) }
                    downButton.setEnableState(
                        isRangeValid(state.value.currentGlucoseLevel, state.value.startGlucoseLevel)
                    )
                }
        }
    }

    private fun initProfileValue() {
        launch {
            getProfileUseCase()
                .onStart { reduceState { state.value.copy(isLoading = true) } }
                .onCompletion { reduceState { state.value.copy(isLoading = false) } }
                .collect { profile ->
                    val glucoseLevel = getProfileGlucoseLevel(profile)
                    reduceState {
                        state.value.copy(profile = profile, startGlucoseLevel = glucoseLevel)
                    }

                    minBeforeMeal.setText(glucoseLevel.beforeMeal.minLevel)
                    maxBeforeMeal.setText(glucoseLevel.beforeMeal.maxLevel)
                    minAfterMeal.setText(glucoseLevel.afterMeal.minLevel)
                    maxAfterMeal.setText(glucoseLevel.afterMeal.maxLevel)
                }
        }
    }

    private fun checkRange() {
        val beforeMeal = state.value.currentGlucoseLevel.beforeMeal
        val afterMeal = state.value.currentGlucoseLevel.afterMeal

        val errorTypeBeforeMeal =
            getErrorTypeByValues(beforeMeal.minLevel, beforeMeal.maxLevel)
        val errorTypeAfterMeal =
            getErrorTypeByValues(afterMeal.minLevel, afterMeal.maxLevel)

        reduceState {
            state.value.copy(
                errorTypeBeforeMeal = errorTypeBeforeMeal,
                errorTypeAfterMeal = errorTypeAfterMeal
            )
        }
    }

    private fun saveRange() {
        val glucoseLevel = state.value.currentGlucoseLevel
        val glucoseLevelSettings = createGlucoseLevelSettings(
            getMinLevel(glucoseLevel.beforeMeal.minLevel, glucoseLevel.afterMeal.minLevel),
            getMaxLevel(glucoseLevel.beforeMeal.maxLevel, glucoseLevel.afterMeal.maxLevel)
        )
        val glucoseLevelBeforeEatSettings = createGlucoseLevelSettings(
            minLevel = glucoseLevel.beforeMeal.minLevel,
            maxLevel = glucoseLevel.beforeMeal.maxLevel
        )
        val glucoseLevelAfterEatSettings = createGlucoseLevelSettings(
            minLevel = glucoseLevel.afterMeal.minLevel,
            maxLevel = glucoseLevel.afterMeal.maxLevel
        )
        val currentProfile = state.value.profile?.copy(
            glucoseLevelSettings = glucoseLevelSettings,
            glucoseLevelBeforeEatSettings = glucoseLevelBeforeEatSettings,
            glucoseLevelAfterEatSettings = glucoseLevelAfterEatSettings
        )
        currentProfile?.let {
            bus.event(Events.ProfileChanged(it))
        }
        router.exit()
    }

    private fun getProfileGlucoseLevel(profile: Profile): GlucoseLevel {
        val glucoseLevelBeforeEatSettings =
            profile.glucoseLevelBeforeEatSettings.normal.toGlucoseRange()
        val glucoseLevelAfterEatSettings =
            profile.glucoseLevelAfterEatSettings.normal.toGlucoseRange()

        return GlucoseLevel(
            beforeMeal = glucoseLevelBeforeEatSettings,
            afterMeal = glucoseLevelAfterEatSettings
        )
    }

    private fun createGlucoseLevelSettings(minLevel: String, maxLevel: String) =
        GlucoseLevelSettings.fromNormalValues(
            normalStart = minLevel.toDoubleFormat() ?: GlucoseLevelSettings.NORMAL_START,
            normalEnd = maxLevel.toDoubleFormat() ?: GlucoseLevelSettings.NORMAL_END
        )
}
