package com.elta.android.presentation.features.calcutator.viewmodel

import com.elta.android.domain.features.user.model.Profile
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseWidgetModel
import com.elta.android.presentation.core.compose.common.Event
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.compose.widgets.BaseAppTopBarWidgetModel
import com.elta.android.presentation.core.compose.widgets.buttons.DownButtonClick
import com.elta.android.presentation.core.compose.widgets.buttons.DownButtonWidgetModel
import com.elta.android.presentation.core.compose.widgets.textfields.SearchFieldWidgetModel
import com.elta.android.presentation.core.compose.widgets.textfields.SearchFocusChanged
import com.elta.android.presentation.features.calcutator.model.CalculatorAction
import com.elta.android.presentation.features.calcutator.model.CalculatorState
import com.elta.android.presentation.features.calcutator.model.DishUi
import javax.inject.Inject
import kotlin.random.Random

// TODO Убрать в дальнейшем. Моковые данные для проверки верстки
private val dishes = (0..50).map {
    DishUi(
        id = it.toString(),
        name = "name ".repeat(Random.nextInt(10)) + it,
        portionDescription = "ration $it",
        portionCount = it.toDouble(),
        isVerification = Random.nextBoolean(),
        calories = it * 12,
        proteins = it * 3,
        fats = it * 4,
        carbs = it * 5,
        breadUnits = it * 3.2
    )
}

class CalculatorViewModel @Inject constructor() :
    BaseViewModel<CalculatorState, Event, CalculatorAction>() {
    override fun createInitState(): CalculatorState =
        CalculatorState(
            profile = Profile(),
            dishes = dishes,
            helpText = "",
            searchInFocus = false,
            lastWords = listOf(
                "Test",
                "Word"
            ),
            findingDishes = dishes
        )

    val appTopBarWidgetModel = BaseAppTopBarWidgetModel()
    val searchFieldWidgetModel = SearchFieldWidgetModel()
    val downButtonWidgetModel = DownButtonWidgetModel()

    fun setHelpText(text: String) {
        reduceState { state.value.copy(helpText = text) }
    }

    fun lastWordOnClick(word: String) {
        sendAction(CalculatorAction.LastWordClick(word))
    }

    fun dishOnClick(dish: DishUi) {
        sendAction(CalculatorAction.DishClick(dish))
    }

    override val widgets: List<BaseWidgetModel<*>> = listOf(
        appTopBarWidgetModel,
        searchFieldWidgetModel,
        downButtonWidgetModel
    ).actionObserve()

    override fun reduceStateByAction(
        currentState: CalculatorState,
        action: Action
    ): CalculatorState =
        when (action) {
            is SearchFocusChanged -> {
                currentState.copy(searchInFocus = action.focusState.isFocused)
            }

            else -> {
                when (action) {
                    is CalculatorAction.LastWordClick ->
                        searchFieldWidgetModel.setText(action.word)

                    is CalculatorAction.DishClick ->
                        router.navigateTo(Screens.AddDishScreen(action.dish))

                    AppAction.BackPressure -> router.exit()

                    // TODO Обработка клика по кнопке Сохранить.
                    DownButtonClick -> {}
                }
                currentState
            }
        }
}
