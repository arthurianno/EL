package com.elta.android.presentation.features.calcutator.viewmodel

import com.elta.android.common.utils.findOrFirst
import com.elta.android.domain.features.calculator.interactor.AddDishFragmentResultHandler
import com.elta.android.domain.features.calculator.interactor.GetFatSecretDishUseCase
import com.elta.android.domain.features.calculator.model.DishType
import com.elta.android.domain.features.user.interactor.round
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseWidgetModel
import com.elta.android.presentation.core.compose.mapDistinct
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.compose.widgets.buttons.DownButtonClick
import com.elta.android.presentation.core.compose.widgets.buttons.DownButtonWidgetModel
import com.elta.android.presentation.core.compose.widgets.dialogs.BaseDialogWidgetModel
import com.elta.android.presentation.core.compose.widgets.textfields.IconTextFieldWidgetModel
import com.elta.android.presentation.features.calcutator.model.CalculatorAction
import com.elta.android.presentation.features.calcutator.model.DishDetailViewState
import com.elta.android.presentation.features.calcutator.model.DishUiEntity
import com.elta.android.presentation.features.calcutator.model.emptyServing
import com.elta.android.presentation.features.calcutator.model.toDomain
import com.elta.android.presentation.features.calcutator.model.toUi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal const val MAX_BREAD_UNITS = 99.9
private const val START_AMOUNT = 1.0
private const val ZERO_COUNT = 0.0
private const val PORTION_COUNT_REGEX = "^(\\d{1,4})(?:[.|,]\\d{0,2})?"
private const val DIGIT_DOT_ALLOWED_CHAR = ','
private const val DIGIT_DOT = '.'

class DishDetailViewModel @Inject constructor(
    private val getFatSecretDish: GetFatSecretDishUseCase,
    private val addDishFragmentResult: AddDishFragmentResultHandler
) : BaseViewModel<DishDetailViewState, Action>() {
    override fun createInitState(): DishDetailViewState =
        DishDetailViewState(
            dish = DishUiEntity(
                id = "",
                localId = "",
                name = "",
                type = DishType.Brand,
                brandName = "",
                isVerification = false,
                servings = emptyList(),
                servingSelect = emptyServing(),
                servingAmount = ZERO_COUNT,
                breadUnits = ZERO_COUNT
            ),
            isShowCountHelpSnack = false
        )

    val downButton = DownButtonWidgetModel()
    val portionCountTextField = IconTextFieldWidgetModel()
    val portionDescriptionTextField = IconTextFieldWidgetModel()
    val warningMaxBreadUnitsDialog = BaseDialogWidgetModel<Nothing>()

    init {
        launch {
            portionCountTextField.state
                .mapDistinct { it.text.replace(DIGIT_DOT_ALLOWED_CHAR, DIGIT_DOT) }
                .map { it.takeIf { it.isNotEmpty() } }
                .map { it?.toDouble() ?: ZERO_COUNT }
                .catch { emit(ZERO_COUNT) }
                .collect {
                    reduceState {
                        state.value.copy(
                            dish = state.value.dish.copy(
                                servingAmount = it,
                                breadUnits = calculateBreadUnits(amount = it)
                            )
                        )
                    }
                }
        }
        launch {
            portionDescriptionTextField.state
                .mapDistinct { it.text }
                .filter { it.isNotEmpty() }
                .map {
                    state.value.dish.servings.first { servingUi -> servingUi.servingDescription == it }
                }
                .collectLatest {
                    portionCountTextField.setText(it.numberOfUnits.toString())
                    reduceState {
                        state.value.copy(
                            dish = state.value.dish.copy(
                                servingSelect = it,
                                breadUnits = calculateBreadUnits(carbs = it.carbs),
                                servingAmount = it.numberOfUnits
                            )
                        )
                    }
                }
        }
        portionCountTextField.textFilter = { text ->
            text.takeIf { text.matches(Regex(PORTION_COUNT_REGEX)) || text.isEmpty() }
        }
    }

    override val widgets: List<BaseWidgetModel<*>> =
        listOf(
            downButton,
            portionCountTextField,
            portionDescriptionTextField
        ).actionObserve()

    fun setDish(dish: DishUiEntity) {
        launch {
            getFatSecretDish(dish.id, dish.type)
                .catch { handleError(it) }
                .map { it.toUi() }
                .map {
                    it.takeIf { dish.localId.isEmpty() }
                        ?: it.copy(
                            localId = dish.localId,
                            servingSelect = dish.servingSelect,
                            servingAmount = dish.servingAmount,
                            breadUnits = dish.breadUnits
                        )
                }
                .collect { newDish ->
                    reduceState { state.value.copy(dish = newDish) }
                    with(portionDescriptionTextField) {
                        setDropDownList(newDish.servings.map { it.servingDescription })
                        val selectServing =
                            newDish.servingSelect.takeIf { it.servingDescription.isNotEmpty() }
                        selectServing?.let { saveServing ->
                            setText(newDish.servings.findOrFirst { saveServing.id == it.id }.servingDescription)
                        }
                    }
                    portionCountTextField.setText(newDish.servingAmount.toInt().toString())
                }
        }
    }

    override fun reduceStateByAction(
        currentState: DishDetailViewState,
        action: Action
    ): DishDetailViewState =
        run {
            when (action) {
                CalculatorAction.PortionHelpClick -> showPortionHelp(true)
                AppAction.FreeScreenTap -> showPortionHelp(false)
                else -> {
                    when (action) {
                        AppAction.BackPressure -> router.exit()
                        DownButtonClick -> saveDish()
                    }
                    currentState
                }
            }
        }

    private fun showPortionHelp(visibilityState: Boolean): DishDetailViewState = run {
        portionCountTextField.setError(visibilityState)
        portionDescriptionTextField.setError(visibilityState)
        state.value.copy(isShowCountHelpSnack = visibilityState)
    }

    private fun saveDish() {
        launch {
            if (state.value.dish.breadUnits > MAX_BREAD_UNITS) {
                warningMaxBreadUnitsDialog.dialogOpen()
            } else {
                addDishFragmentResult.onNext(state.value.dish.toDomain())
                    .catch { handleError(it) }
                    .collect { router.exit() }
            }
        }
    }

    private fun calculateBreadUnits(carbs: Double? = null, amount: Double? = null): Double {
        val newCarbs = carbs ?: state.value.dish.servingSelect.carbs
        val newAmount =
            amount ?: (portionCountTextField.state.value.text.toDoubleOrNull()) ?: START_AMOUNT
        val portion =
            state.value.dish.servingSelect.numberOfUnits.takeIf { it > ZERO_COUNT } ?: START_AMOUNT
        val breadUnits = (newCarbs * newAmount / (10 * portion)).round(1)
        downButton.setEnableState(breadUnits > ZERO_COUNT)
        return breadUnits
    }
}
