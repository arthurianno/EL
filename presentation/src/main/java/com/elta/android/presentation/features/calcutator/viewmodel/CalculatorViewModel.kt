package com.elta.android.presentation.features.calcutator.viewmodel

import com.elta.android.domain.features.calculator.interactor.AddDishFragmentResultHandler
import com.elta.android.domain.features.calculator.interactor.CalculatorFragmentResultHandler
import com.elta.android.domain.features.calculator.interactor.GetHistoryListUseCase
import com.elta.android.domain.features.calculator.interactor.SaveWordToHistoryUseCase
import com.elta.android.domain.features.calculator.interactor.SearchDishesUseCase
import com.elta.android.domain.features.user.interactor.round
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
import com.elta.android.presentation.core.compose.widgets.dialogs.BaseDialogWidgetModel
import com.elta.android.presentation.core.compose.widgets.textfields.SearchFieldWidgetModel
import com.elta.android.presentation.core.compose.widgets.textfields.SearchFocusChanged
import com.elta.android.presentation.features.calcutator.model.CalculatorAction
import com.elta.android.presentation.features.calcutator.model.CalculatorState
import com.elta.android.presentation.features.calcutator.model.DishUi
import com.elta.android.presentation.features.calcutator.model.toDomain
import com.elta.android.presentation.features.calcutator.model.toUi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class CalculatorViewModel @Inject constructor(
    private val searchDishes: SearchDishesUseCase,
    private val getHistoryList: GetHistoryListUseCase,
    private val saveWordToHistory: SaveWordToHistoryUseCase,
    private val addDishFragmentResult: AddDishFragmentResultHandler,
    private val calculatorFragmentResult: CalculatorFragmentResultHandler
) :
    BaseViewModel<CalculatorState, Event, CalculatorAction>() {
    override fun createInitState(): CalculatorState =
        CalculatorState(
            profile = Profile(),
            dishes = emptyList(),
            totalBreadUnits = 0.0,
            helpText = "",
            searchInFocus = false,
            lastWords = emptyList(),
            findingDishes = emptyList(),
            isFindDishes = false
        )

    val appTopBar = BaseAppTopBarWidgetModel()
    val searchField = SearchFieldWidgetModel().also {
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
    val downButton = DownButtonWidgetModel()
    val dishDeleteConfirmDialog = BaseDialogWidgetModel<DishUi>(
        positiveOnCLick = { deletedDish -> deletedDish?.let { deleteDish(it) } }
    )

    init {
        launch {
            addDishFragmentResult.resultAsFlow()
                .catch { handleError(it) }
                .collect { editDishes(it.toUi()) }
        }
        launch {
            getHistoryList()
                .catch { handleError(it) }
                .collectLatest { reduceState { state.value.copy(lastWords = it) } }
        }
    }

    override val widgets: List<BaseWidgetModel<*>> = listOf(
        appTopBar,
        searchField,
        downButton
    ).actionObserve()

    fun setHelpText(text: String) {
        reduceState { state.value.copy(helpText = text) }
    }

    fun lastWordOnClick(word: String) {
        sendAction(CalculatorAction.LastWordClick(word))
    }

    fun addDishOnClick(dish: DishUi) {
        sendAction(CalculatorAction.AddDishClick(dish))
    }

    fun dishCardOnClick(dishUi: DishUi) {
        dishClick(dish = dishUi, isNewDish = false)
    }

    fun dishDeleteOnClick(dishUi: DishUi) {
        dishDeleteConfirmDialog.dialogOpen(dishUi)
    }

    fun loadDishes(eventId: String) {
        // TODO реализовать получение продуктов с нашего АПИ
    }

    override fun reduceStateByAction(
        currentState: CalculatorState,
        action: Action
    ): CalculatorState =
        when (action) {
            is SearchFocusChanged -> {
                val visibilityState = action.focusState.isFocused
                downButton.visibilityState(visibilityState)
                currentState.copy(searchInFocus = visibilityState)
            }

            else -> {
                when (action) {
                    is CalculatorAction.LastWordClick -> searchField.setText(action.word)
                    is CalculatorAction.AddDishClick -> dishClick(action.dish)
                    AppAction.BackPressure -> router.exit()
                    DownButtonClick -> saveDishes()
                }
                currentState
            }
        }

    private fun setDishes(dishes: List<DishUi>) {
        reduceState {
            state.value.copy(
                dishes = dishes,
                totalBreadUnits = dishes.sumOf { it.breadUnits }.round(1)
            )
        }
    }

    private fun deleteDish(dish: DishUi) {
        val newDishes = state.value.dishes
            .toMutableList()
            .apply { remove(dish) }
        reduceState { state.value.copy(dishes = newDishes) }
    }

    private fun editDishes(dish: DishUi) {
        val newDishes = state.value.dishes
            .toMutableList()
            .apply {
                find { it.localId == dish.localId }?.let {
                    val indexDish = indexOf(it)
                    remove(it)
                    add(indexDish, dish)
                } ?: add(dish)
            }
            .toList()
        setDishes(newDishes)
    }

    private fun saveDishes() {
        launch {
            calculatorFragmentResult.onNext(state.value.dishes.toDomain())
                .catch { handleError(it) }
                .collect { router.exit() }
        }
    }

    private fun dishClick(dish: DishUi, isNewDish: Boolean = true) {
        launch {
            saveWordToHistory(dish.name)
                .catch { handleError(it) }
                .collect {
                    getHistoryList()
                        .catch { handleError(it) }
                        .collectLatest {
                            reduceState { state.value.copy(lastWords = it) }
                            router.navigateTo(Screens.AddDishScreen(dish, isNewDish))
                        }
                }
        }
    }

    private fun clearFindingDishes() {
        reduceState { state.value.copy(findingDishes = emptyList()) }
    }

    private fun findDishes(name: String) {
        launch {
            searchDishes(name)
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
