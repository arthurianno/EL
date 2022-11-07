package com.elta.android.presentation.features.calcutator.viewmodel

import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseWidgetModel
import com.elta.android.presentation.core.compose.common.Event
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.compose.widgets.buttons.DownButtonWidgetModel
import com.elta.android.presentation.core.compose.widgets.textfields.IconTextFieldWidgetModel
import com.elta.android.presentation.features.calcutator.model.DishState
import com.elta.android.presentation.features.calcutator.model.DishUi
import javax.inject.Inject

// TODO Убрать моковые данные
private val descriptions = listOf(
    "Очень небольшой (менее 15  см в длину)",
    "Маленький (105 гр)",
    "Средний (105 гр)",
    "Большой (105 гр)",
    "Чашка",
    "Грамм"
)

class AddDishViewModel @Inject constructor() : BaseViewModel<DishState, Event, Action>() {
    override fun createInitState(): DishState =
        DishState(
            dish = DishUi(
                id = "",
                name = "",
                portionDescription = "",
                portionCount = 0.0,
                isVerification = false,
                calories = 0,
                proteins = 0,
                fats = 0,
                carbs = 0,
                breadUnits = 0.0
            ),
            dishPortionDescriptions = descriptions
        )

    val downButtonWidgetModel = DownButtonWidgetModel()
    val portionCountTextField = IconTextFieldWidgetModel()
    val portionDescriptionTextField = IconTextFieldWidgetModel()

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

    fun setDish(dish: DishUi) {
        reduceState { state.value.copy(dish = dish) }
    }
}
