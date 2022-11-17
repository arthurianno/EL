package com.elta.android.presentation.features.calcutator.viewmodel

import com.elta.android.domain.features.calculator.interactor.AddDishFragmentResultHandler
import com.elta.android.domain.features.calculator.interactor.GetDishUseCase
import com.elta.android.domain.features.calculator.model.DishType
import com.elta.android.domain.features.user.interactor.round
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseWidgetModel
import com.elta.android.presentation.core.compose.common.Event
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.compose.widgets.buttons.DownButtonClick
import com.elta.android.presentation.core.compose.widgets.buttons.DownButtonWidgetModel
import com.elta.android.presentation.core.compose.widgets.textfields.IconTextFieldWidgetModel
import com.elta.android.presentation.features.calcutator.model.AddDishState
import com.elta.android.presentation.features.calcutator.model.DishUi
import com.elta.android.presentation.features.calcutator.model.servingUiEmpty
import com.elta.android.presentation.features.calcutator.model.toDomain
import com.elta.android.presentation.features.calcutator.model.toUi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private const val START_AMOUNT = 1.0

class AddDishViewModel @Inject constructor(
    private val getDish: GetDishUseCase,
    private val addDishFragmentResult: AddDishFragmentResultHandler
) : BaseViewModel<AddDishState, Event, Action>() {
    override fun createInitState(): AddDishState =
        AddDishState(
            dish = DishUi(
                id = "",
                localId = "",
                name = "",
                type = DishType.Brand,
                isVerification = false,
                servings = emptyList(),
                servingSelect = servingUiEmpty(),
                servingAmount = START_AMOUNT,
                breadUnits = 0.0
            )
        )

    val downButtonWidgetModel = DownButtonWidgetModel()
    val portionCountTextField = IconTextFieldWidgetModel().also { portionCount ->
        launch {
            portionCount.state
                .map { it.text }
                .filter { it.isNotEmpty() }
                .map { it.toDouble() }
                .collectLatest {
                    reduceState {
                        state.value.copy(
                            dish = state.value.dish.copy(
                                servingAmount = it,
                                breadUnits = getBreadUnits(amount = it)
                            )
                        )
                    }
                }
        }
    }

    val portionDescriptionTextField = IconTextFieldWidgetModel().also { portionField ->
        launch {
            portionField.state
                .map { it.text }
                .filter { it.isNotEmpty() }
                .map {
                    state.value.dish.servings.first { servingUi -> servingUi.measurementDescription == it }
                }
                .collectLatest {
                    reduceState {
                        state.value.copy(
                            dish = state.value.dish.copy(
                                servingSelect = it,
                                breadUnits = getBreadUnits(carbs = it.carbs),
                                servingAmount = it.numberOfUnits
                            )
                        )
                    }
                }
        }
    }

    override val widgets: List<BaseWidgetModel<*>> =
        listOf(
            downButtonWidgetModel,
            portionCountTextField,
            portionDescriptionTextField
        ).actionObserve()

    override fun reduceStateByAction(currentState: AddDishState, action: Action): AddDishState =
        run {
            when (action) {
                AppAction.BackPressure -> router.exit()
                DownButtonClick -> saveDish()
            }
            currentState
        }

    private fun saveDish() {
        launch {
            addDishFragmentResult.onNext(state.value.dish.toDomain())
                .catch { handleError(it) }
                .collect { router.exit() }
        }
    }

    fun setDish(dishUi: DishUi, isNewDish: Boolean) {
        launch {
            if (isNewDish) {
                getDish(dishUi.toDomain())
                    .catch { handleError(it) }
                    .map { it.toUi() }
                    .collect { dish ->
                        reduceState { state.value.copy(dish = dish) }
                        portionDescriptionTextField.setDropDownList(dish.servings.map { it.measurementDescription })
                    }
            } else {
                reduceState { state.value.copy(dish = dishUi) }
                with(portionDescriptionTextField) {
                    setDropDownList(dishUi.servings.map { it.measurementDescription })
                    setText(dishUi.servingSelect.measurementDescription)
                }
                portionCountTextField.setText(dishUi.servingAmount.toInt().toString())
            }
        }
    }

    private fun getBreadUnits(carbs: Double? = null, amount: Double? = null): Double {
        val newCarbs = carbs ?: state.value.dish.servingSelect.carbs
        val newAmount =
            amount ?: (portionCountTextField.state.value.text.toDoubleOrNull()) ?: START_AMOUNT
        val portion = state.value.dish.servingSelect.numberOfUnits.takeIf { it > 0.0 } ?: 1.0
        return (newCarbs * newAmount / (10 * portion)).round(1)
    }
}
