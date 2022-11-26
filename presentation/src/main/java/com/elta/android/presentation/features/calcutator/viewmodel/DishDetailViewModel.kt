package com.elta.android.presentation.features.calcutator.viewmodel

import com.elta.android.domain.features.calculator.interactor.AddDishFragmentResultHandler
import com.elta.android.domain.features.calculator.interactor.GetFatSecretDishUseCase
import com.elta.android.domain.features.calculator.model.DishType
import com.elta.android.domain.features.user.interactor.round
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseWidgetModel
import com.elta.android.presentation.core.compose.common.Event
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.compose.widgets.buttons.DownButtonClick
import com.elta.android.presentation.core.compose.widgets.buttons.DownButtonWidgetModel
import com.elta.android.presentation.core.compose.widgets.dialogs.BaseDialogWidgetModel
import com.elta.android.presentation.core.compose.widgets.textfields.IconTextFieldWidgetModel
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

private const val START_AMOUNT = 1.0
private const val MAX_BREAD_UNITS = 99.9

class DishDetailViewModel @Inject constructor(
    private val getFatSecretDish: GetFatSecretDishUseCase,
    private val addDishFragmentResult: AddDishFragmentResultHandler
) : BaseViewModel<DishDetailViewState, Event, Action>() {
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
                servingAmount = START_AMOUNT,
                breadUnits = 0.0
            )
        )

    val downButton = DownButtonWidgetModel()
    val portionCountTextField = IconTextFieldWidgetModel()
    val portionDescriptionTextField = IconTextFieldWidgetModel()
    val warningMaxBreadUnitsDialog = BaseDialogWidgetModel<Nothing>()

    init {
        launch {
            portionCountTextField.state
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
        launch {
            portionDescriptionTextField.state
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
            downButton,
            portionCountTextField,
            portionDescriptionTextField
        ).actionObserve()

    override fun reduceStateByAction(
        currentState: DishDetailViewState,
        action: Action
    ): DishDetailViewState =
        run {
            when (action) {
                AppAction.BackPressure -> router.exit()
                DownButtonClick -> saveDish()
            }
            currentState
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
                        setDropDownList(newDish.servings.map { it.measurementDescription })
                        newDish.servingSelect.measurementDescription.takeIf { it.isNotEmpty() }
                            ?.let { setText(it) }
                    }
                    portionCountTextField.setText(newDish.servingAmount.toInt().toString())
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
