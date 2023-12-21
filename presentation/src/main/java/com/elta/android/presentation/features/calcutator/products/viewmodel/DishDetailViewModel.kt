package com.elta.android.presentation.features.calcutator.products.viewmodel

import com.elta.android.common.utils.findOrFirst
import com.elta.android.domain.common.mapDistinct
import com.elta.android.domain.features.calculator.interactor.AddDishFragmentResultHandler
import com.elta.android.domain.features.calculator.interactor.GetDishUseCase
import com.elta.android.domain.features.calculator.model.DishType
import com.elta.android.domain.features.diary.home.model.CalculatorFlow
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseWidgetModel
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBarWidgetModel
import com.elta.android.presentation.core.compose.widgets.buttons.DownButtonClick
import com.elta.android.presentation.core.compose.widgets.buttons.DownButtonWidgetModel
import com.elta.android.presentation.core.compose.widgets.dialogs.BaseDialogWidgetModel
import com.elta.android.presentation.core.compose.widgets.dialogs.InfoDialogWidgetModel
import com.elta.android.presentation.core.compose.widgets.textfields.DropdownFieldWidgetModel
import com.elta.android.presentation.core.compose.widgets.textfields.IconOutlinedTextFieldWidgetModel
import com.elta.android.presentation.features.calcutator.mappers.ZERO_COUNT_DOUBLE
import com.elta.android.presentation.features.calcutator.mappers.ZERO_COUNT_INT
import com.elta.android.presentation.features.calcutator.mappers.breadUnitsIsMax
import com.elta.android.presentation.features.calcutator.mappers.calculateBreadUnits
import com.elta.android.presentation.features.calcutator.mappers.emptyServing
import com.elta.android.presentation.features.calcutator.mappers.toCalculate
import com.elta.android.presentation.features.calcutator.mappers.toDomain
import com.elta.android.presentation.features.calcutator.mappers.toNewAmount
import com.elta.android.presentation.features.calcutator.mappers.toUi
import com.elta.android.presentation.features.calcutator.products.model.CalculatorAction
import com.elta.android.presentation.features.calcutator.products.model.DishDetailAction
import com.elta.android.presentation.features.calcutator.products.model.DishDetailViewState
import com.elta.android.presentation.features.calcutator.products.model.DishUiEntity
import com.elta.android.presentation.features.calcutator.products.model.ServingUiEntity
import com.elta.android.presentation.features.profile.settings.dialogs.glucose.model.COMMA_CHAR
import com.elta.android.presentation.features.profile.settings.dialogs.glucose.model.DOT_CHAR
import com.elta.android.presentation.utils.PORTION_COUNT_INTEGER_PART
import com.elta.android.presentation.utils.PORTION_INTEGER_LENGTH_REGEX
import com.elta.android.presentation.utils.PORTION_VALUE_REGEX
import com.elta.android.presentation.utils.createTextFilterForDoubleValue
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

internal const val TWO_DECIMAL_PLACES = 2
internal const val DIGIT_DOT_ALLOWED_CHAR = ','
internal const val DIGIT_DOT = '.'
internal const val PATTERN_ZERO_AFTER_DECIMAL = "0.##"
internal const val NOTHING_DASH = "—"
internal const val DIGIT_ZERO_STRING = "0"
internal const val EMPTY_STRING = ""
private const val START_AMOUNT = 1.0

class DishDetailViewModel @Inject constructor(
    private val getDish: GetDishUseCase,
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
                isVerified = false,
                servings = emptyList(),
                servingSelect = emptyServing(),
                servingAmount = "0.0",
                servingCalories = Pair("0", "0"),
                breadUnits = "0.0"
            ),
            startDish = DishUiEntity(
                id = "",
                localId = "",
                name = "",
                type = DishType.Brand,
                brandName = "",
                isVerified = false,
                servings = emptyList(),
                servingSelect = emptyServing(),
                servingAmount = "0.0",
                servingCalories = Pair("0", "0"),
                breadUnits = "0.0"
            ),
            isShowCountHelpSnack = false,
            calculatorFlow = CalculatorFlow.BREAD_UNITS,
            isLoading = true,
            isError = false,
        )

    val appTopBar = BaseAppTopBarWidgetModel()
    val downButton = DownButtonWidgetModel()
    val portionCountTextField = IconOutlinedTextFieldWidgetModel()
    val portionDescriptionDropdownField = DropdownFieldWidgetModel()
    val warningMaxBreadUnitsDialog = BaseDialogWidgetModel<Nothing>()
    val warningExitDialog = BaseDialogWidgetModel<Nothing>(positiveOnCLick = {
        router.backTo(Screens.CalculatorScreen(state.value.calculatorFlow))
    })

    val viewNameDialog = InfoDialogWidgetModel<Nothing>(onCLick = {})

    override val widgets: List<BaseWidgetModel<*>> =
        listOf(
            appTopBar,
            downButton,
            portionCountTextField,
            portionDescriptionDropdownField
        ).actionObserve()

    init {

        launch {
            portionCountTextField.state
                .mapDistinct { it.textField.text.replace(DIGIT_DOT_ALLOWED_CHAR, DIGIT_DOT) }
                .map { it.takeIf { it.isNotEmpty() } }
                .map { it?.toDouble() ?: ZERO_COUNT_DOUBLE }
                .catch { emit(ZERO_COUNT_DOUBLE) }
                .collect { amount ->
                    val breadUnits = calculate(amount = amount)
                    reduceState {
                        state.value.copy(
                            dish = state.value.dish.copy(
                                servingAmount = amount.toString(),
                                breadUnits = breadUnits.toString(),
                                servingSelect = calculateServing(amount = amount)
                            )
                        )
                    }
                    downButton.setEnableState(
                        downButtonIsEnabled(
                            breadUnits,
                            state.value.calculatorFlow,
                            amount
                        )
                    )
                }
        }
        launch {
            portionDescriptionDropdownField.state
                .mapDistinct { it.text }
                .filter { it.isNotEmpty() }
                .map {
                    state.value.dish.servings.first { servingUi -> servingUi.nameMetricServing == it }
                }
                .collectLatest {
                    reduceState {
                        portionCountTextField.setText(it.numberOfUnits)
                        state.value.copy(
                            dish = state.value.dish.copy(
                                servingSelect = it,
                                breadUnits = calculate(carbs = it.carbohydrate?.toDouble()).toString(),
                                servingAmount = it.numberOfUnits
                            )
                        )
                    }
                }
        }
        portionCountTextField.textFilter = createTextFilterForDoubleValue(
            PORTION_INTEGER_LENGTH_REGEX,
            PORTION_VALUE_REGEX,
            PORTION_COUNT_INTEGER_PART
        )
    }

    private fun downButtonIsEnabled(
        breadUnits: Double,
        calculatorFlow: CalculatorFlow,
        amount: Double
    ): Boolean =
        when (calculatorFlow) {
            CalculatorFlow.BREAD_UNITS -> {
                breadUnits > ZERO_COUNT_DOUBLE
            }

            CalculatorFlow.PRODUCT_ONLY -> {
                amount > ZERO_COUNT_INT
            }
        }


    fun setDish(dish: DishUiEntity) {
        launch {
            getDish(dish.id, dish.type)
                .catch {
                    reduceState { state.value.copy(isError = true) }
                    handleError(it)
                }
                .onStart {
                    reduceState {
                        val servingSelect =
                            if (dish.servingSelect.id.isEmpty()) dish.servings.first()
                            else dish.servingSelect

                        state.value.copy(
                            startDish = dish.copy(servingSelect = servingSelect),
                            isLoading = true,
                            isError = false
                        )
                    }
                }
                .onCompletion { reduceState { state.value.copy(isLoading = false) } }
                .map { it.toUi() }
                .map {
                    it.takeIf { dish.localId.isEmpty() || dish.servingSelect.id.isBlank() }
                        ?: it.copy(
                            localId = dish.localId,
                            servingSelect = dish.servingSelect,
                            servingAmount = dish.servingAmount,
                            breadUnits = dish.breadUnits
                        )
                }
                .collect { newDish ->
                    reduceState {
                        state.value.copy(
                            dish = newDish,
                            isError = false,
                        )
                    }
                    portionDescriptionDropdownField.setDropDownList(newDish.servings.map { it.nameMetricServing })
                    val selectServing =
                        newDish.servingSelect.takeIf { it.nameMetricServing.isNotEmpty() }
                    selectServing?.let { saveServing ->
                        portionDescriptionDropdownField.setText(saveServing.nameMetricServing)
                    }
                    val count = selectServing?.numberOfUnits ?: dish.servings.first().numberOfUnits
                    portionCountTextField.setText(count)
                }
        }
    }

    fun setCalculatorFlow(calculatorFlow: CalculatorFlow) {
        reduceState {
            state.value.copy(
                calculatorFlow = calculatorFlow
            )
        }
    }

    override fun handleUserAction(action: Action) {
        when (action) {
            AppAction.BackPressure -> backClick()
            DownButtonClick -> saveDish()
            DishDetailAction.ViewName -> viewNameDialog.dialogOpen(message = state.value.dish.name)
            DishDetailAction.Retry -> setDish(state.value.startDish)
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

    override fun backClick() {
        if (isServingChanged()) warningExitDialog.dialogOpen()
        else router.backTo(Screens.CalculatorScreen(state.value.calculatorFlow))
    }

    private fun showPortionHelp(visibilityState: Boolean): DishDetailViewState = run {
        portionCountTextField.setError(visibilityState)
        portionDescriptionDropdownField.setError(visibilityState)
        state.value.copy(isShowCountHelpSnack = visibilityState)
    }

    private fun saveDish() {
        launch {

            val valueIsMax = if (state.value.calculatorFlow == CalculatorFlow.BREAD_UNITS) {
                state.value.dish.breadUnits?.toDoubleOrNull().breadUnitsIsMax()
            } else {
                false
            }

            if (valueIsMax) {
                warningMaxBreadUnitsDialog.dialogOpen()
            } else {
                addDishFragmentResult.onNext(state.value.dish.toDomain())
                    .catch { handleError(it) }
                    .collect { router.backTo(Screens.CalculatorScreen(state.value.calculatorFlow)) }
            }
        }
    }


    private fun calculate(carbs: Double? = null, amount: Double? = null): Double {
        val serving = getServingOrDefault()

        val numberOfUnits = serving.numberOfUnits

        val newAmount = amount
            ?: (portionCountTextField.state.value.textField.text.toDoubleOrNull())
            ?: START_AMOUNT
        val newCarbs = (carbs ?: serving.carbohydrate?.toDouble())?.toCalculate(
            newAmount,
            numberOfUnits.toDouble()
        )
        return newCarbs?.let { calculateBreadUnits(it) } ?: ZERO_COUNT_DOUBLE
    }

    private fun getServingOrDefault(): ServingUiEntity =
        state.value.dish.servings.find {
            it.id == state.value.dish.servingSelect.id
        } ?: emptyServing()

    private fun calculateServing(amount: Double): ServingUiEntity {
        val dish = state.value.dish
        val serving = if (dish.servingSelect.id.isNotEmpty() && dish.servings.isNotEmpty()) {
            dish.servings.findOrFirst {
                it.id == dish.servingSelect.id
            }
        } else emptyServing()

        return serving.toNewAmount(amount)
    }

    private fun isServingChanged(): Boolean {
        val startDish = state.value.startDish
        val portionCount =
            portionCountTextField.state.value.textField.text.ifEmpty { portionCountTextField.state.value.oldText }

        return (startDish.servingSelect.numberOfUnits.replace(DOT_CHAR, COMMA_CHAR) != portionCount
                || startDish.servingSelect.nameMetricServing != portionDescriptionDropdownField.state.value.text) &&
                state.value.dish.id.isNotBlank()
    }
}
