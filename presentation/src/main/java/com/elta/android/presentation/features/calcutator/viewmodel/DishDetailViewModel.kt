package com.elta.android.presentation.features.calcutator.viewmodel

import com.elta.android.common.utils.findOrFirst
import com.elta.android.domain.common.mapDistinct
import com.elta.android.domain.features.calculator.interactor.AddDishFragmentResultHandler
import com.elta.android.domain.features.calculator.interactor.GetFatSecretDishUseCase
import com.elta.android.domain.features.calculator.model.DishType
import com.elta.android.domain.features.user.interactor.round
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseWidgetModel
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.compose.widgets.buttons.DownButtonClick
import com.elta.android.presentation.core.compose.widgets.buttons.DownButtonWidgetModel
import com.elta.android.presentation.core.compose.widgets.dialogs.BaseDialogWidgetModel
import com.elta.android.presentation.core.compose.widgets.dialogs.InfoDialogWidgetModel
import com.elta.android.presentation.core.compose.widgets.textfields.IconTextFieldWidgetModel
import com.elta.android.presentation.features.calcutator.model.CalculatorAction
import com.elta.android.presentation.features.calcutator.model.DishDetailAction
import com.elta.android.presentation.features.calcutator.model.DishDetailViewState
import com.elta.android.presentation.features.calcutator.model.DishUiEntity
import com.elta.android.presentation.features.calcutator.model.ServingUiEntity
import com.elta.android.presentation.features.calcutator.model.emptyServing
import com.elta.android.presentation.features.calcutator.model.toDomain
import com.elta.android.presentation.features.calcutator.model.toNewAmount
import com.elta.android.presentation.features.calcutator.model.toRoundValue
import com.elta.android.presentation.features.calcutator.model.toUi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal const val MAX_BREAD_UNITS = 99.9
private const val CONVERSION_FACTOR = 10
private const val ONE_DECIMAL_PLACE = 1
private const val TWO_DECIMAL_PLACES = 2
private const val START_AMOUNT = 1.0
private const val ZERO_COUNT = 0.0
private const val PORTION_COUNT_REGEX = "^(\\d{1,4})(?:[.|,]\\d{0,2})?"
private const val DIGIT_DOT_ALLOWED_CHAR = ','
private const val DIGIT_DOT = '.'

class DishDetailViewModel @Inject constructor(
    private val getFatSecretDish: GetFatSecretDishUseCase,
    private val addDishFragmentResult: AddDishFragmentResultHandler
) : BaseViewModel<DishDetailViewState>() {
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

    val viewNameDialog = InfoDialogWidgetModel<Nothing>(onCLick = {})

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
                                breadUnits = calculateBreadUnits(amount = it),
                                servingSelect = calculateServing(amount = it)
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
                    reduceState {
                        state.value.copy(
                            dish = state.value.dish.copy(
                                servingSelect = it.toRoundValue(),
                                breadUnits = calculateBreadUnits(carbs = it.carbs),
                                servingAmount = START_AMOUNT
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
                    portionCountTextField.setText(newDish.servingAmount.toString())
                }
        }
    }

    override fun handleUserAction(action: Action) {
        when (action) {
            AppAction.BackPressure -> router.exit()
            DownButtonClick -> saveDish()
            DishDetailAction.ViewName -> viewNameDialog.dialogOpen(message = state.value.dish.name)
        }
    }

    override fun reduceStateByAction(
        currentState: DishDetailViewState,
        action: Action
    ): DishDetailViewState =
        when (action) {
            CalculatorAction.PortionHelpClick -> showPortionHelp(true)
            AppAction.FreeScreenTap -> showPortionHelp(false)
            else -> currentState
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
        val newCarbs = carbs ?: getServingOrDefault().carbs
        val newAmount =
            amount ?: (portionCountTextField.state.value.text.toDoubleOrNull()) ?: START_AMOUNT
        val breadUnits = (newCarbs * newAmount / CONVERSION_FACTOR).round(ONE_DECIMAL_PLACE)
        downButton.setEnableState(breadUnits > ZERO_COUNT)
        return breadUnits
    }

    private fun getServingOrDefault(): ServingUiEntity =
        state.value.dish.servings.find {
            it.id == state.value.dish.servingSelect.id
        } ?: emptyServing()

    private fun calculateServing(amount: Double? = null): ServingUiEntity {
        val dish = state.value.dish
        val serving = if (dish.servingSelect != emptyServing()){
            dish.servings.findOrFirst {
                it.id == dish.servingSelect.id
            }
        } else emptyServing()
        val amountServing =
            amount ?: (portionCountTextField.state.value.text.toDoubleOrNull()) ?: START_AMOUNT

        return serving.toNewAmount(amountServing).toRoundValue(TWO_DECIMAL_PLACES)
    }
}
