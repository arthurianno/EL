package com.elta.android.presentation.features.calcutator.viewmodel

import com.elta.android.domain.features.calculator.interactor.GetDishUseCase
import com.elta.android.domain.features.calculator.model.DishType
import com.elta.android.domain.features.user.interactor.round
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseWidgetModel
import com.elta.android.presentation.core.compose.common.Event
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.compose.widgets.buttons.DownButtonWidgetModel
import com.elta.android.presentation.core.compose.widgets.textfields.IconTextFieldWidgetModel
import com.elta.android.presentation.features.calcutator.model.DishState
import com.elta.android.presentation.features.calcutator.model.DishUi
import com.elta.android.presentation.features.calcutator.model.PortionUi
import com.elta.android.presentation.features.calcutator.model.toDomain
import com.elta.android.presentation.features.calcutator.model.toUi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private const val START_PORTION = 1.0

class AddDishViewModel @Inject constructor(
    private val getDish: GetDishUseCase
) : BaseViewModel<DishState, Event, Action>() {
    override fun createInitState(): DishState =
        DishState(
            dish = DishUi(
                id = "",
                name = "",
                type = DishType.Brand,
                isVerification = false,
                portions = emptyList(),
                breadUnits = 0.0
            ),
            portion = PortionUi(
                id = "",
                description = "",
                calories = 0.0,
                carbs = 0.0,
                fats = 0.0,
                proteins = 0.0
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
                        state.value.copy(dish = state.value.dish calculateBreadUnits it)
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
                    state.value.dish.portions.first { portionUi -> portionUi.description == it }
                }
                .collectLatest {
                    reduceState {
                        state.value
                            .copy(portion = it)
                            .copy(
                                dish = state.value.dish calculateBreadUnits (
                                    portionCountTextField.state.value.text.toDoubleOrNull()
                                        ?: START_PORTION
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

    override fun reduceStateByAction(currentState: DishState, action: Action): DishState = run {
        when (action) {
            AppAction.BackPressure -> router.exit()
        }
        currentState
    }

    fun setDish(dishUi: DishUi) {
        launch {
            getDish(dishUi.toDomain())
                .catch { handleError(it) }
                .map { it.toUi() }
                .collect { dish ->
                    reduceState {
                        state.value.copy(
                            dish = dish,
                            portion = dish.portions.first()
                        )
                    }
                    portionDescriptionTextField.setDropDownList(dish.portions.map { it.description })
                }
        }
    }

    private infix fun DishUi.calculateBreadUnits(portionCount: Double): DishUi =
        copy(breadUnits = (state.value.portion.carbs * portionCount / 10).round(1))
}
