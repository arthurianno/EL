package com.elta.android.presentation.features.calcutator.viewmodel

import com.elta.android.domain.features.calculator.interactor.GetDishesUseCase
import com.elta.android.domain.features.calculator.interactor.GetHistoryListUseCase
import com.elta.android.domain.features.calculator.interactor.SaveWordToHistoryUseCase
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
import com.elta.android.presentation.features.calcutator.model.toUi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class CalculatorViewModel @Inject constructor(
    private val getDishes: GetDishesUseCase,
    private val getHistoryList: GetHistoryListUseCase,
    private val saveWordToHistory: SaveWordToHistoryUseCase
) :
    BaseViewModel<CalculatorState, Event, CalculatorAction>() {
    override fun createInitState(): CalculatorState =
        CalculatorState(
            profile = Profile(),
            dishes = emptyList(),
            helpText = "",
            searchInFocus = false,
            lastWords = emptyList(),
            findingDishes = emptyList(),
            isFindDishes = false
        )

    val appTopBarWidgetModel = BaseAppTopBarWidgetModel()
    val searchFieldWidgetModel = SearchFieldWidgetModel().also {
        launch {
            it.state
                .map { it.text }
                .collectLatest {
                    if (it.isNotEmpty()) {
                        findDishes(it)
                    } else {
                        clearFindingDishes()
                    }
                }
        }
    }
    val downButtonWidgetModel = DownButtonWidgetModel()

    override val widgets: List<BaseWidgetModel<*>> = listOf(
        appTopBarWidgetModel,
        searchFieldWidgetModel,
        downButtonWidgetModel
    ).actionObserve()

    fun setHelpText(text: String) {
        reduceState { state.value.copy(helpText = text) }
    }

    fun lastWordOnClick(word: String) {
        sendAction(CalculatorAction.LastWordClick(word))
    }

    fun dishOnClick(dish: DishUi) {
        sendAction(CalculatorAction.DishClick(dish))
    }

    override fun reduceStateByAction(
        currentState: CalculatorState,
        action: Action
    ): CalculatorState =
        when (action) {
            is SearchFocusChanged -> currentState.copy(searchInFocus = action.focusState.isFocused)

            else -> {
                when (action) {
                    is CalculatorAction.LastWordClick -> searchFieldWidgetModel.setText(action.word)
                    is CalculatorAction.DishClick -> dishClick(action.dish)
                    AppAction.BackPressure -> router.exit()
                    // TODO Обработка клика по кнопке Сохранить.
                    DownButtonClick -> {}
                }
                currentState
            }
        }

    private fun dishClick(dish: DishUi) {
        launch {
            saveWordToHistory(dish.name)
                .catch { handleError(it) }
                .collect {
                    getHistoryList()
                        .catch { handleError(it) }
                        .collectLatest {
                            reduceState { state.value.copy(lastWords = it) }
                            router.navigateTo(Screens.AddDishScreen(dish))
                        }
                }
        }
    }

    private fun clearFindingDishes() {
        reduceState { state.value.copy(findingDishes = emptyList()) }
    }

    private fun findDishes(name: String) {
        launch {
            getDishes(name)
                .catch { handleError(it) }
                .onStart { reduceState { state.value.copy(isFindDishes = true) } }
                .onCompletion { reduceState { state.value.copy(isFindDishes = false) } }
                .map { it.toUi() }
                .collectLatest { dishes ->
                    reduceState {
                        state.value.copy(
                            findingDishes = dishes,
                            isFindDishes = false
                        )
                    }
                }
        }
    }
}
