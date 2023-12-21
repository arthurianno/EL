package com.elta.android.presentation.features.calcutator.products.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.elta.android.common.errors.ServiceUnavailableError
import com.elta.android.domain.features.calculator.interactor.AddDishFragmentResultHandler
import com.elta.android.domain.features.calculator.interactor.CalculatorFragmentResultHandler
import com.elta.android.domain.features.calculator.interactor.GetCachedDishesUseCase
import com.elta.android.domain.features.calculator.interactor.GetHistoryListUseCase
import com.elta.android.domain.features.calculator.interactor.SaveWordToHistoryUseCase
import com.elta.android.domain.features.calculator.interactor.SearchDishesInFatSecretUseCase
import com.elta.android.domain.features.calculator.interactor.SearchProductUseCase
import com.elta.android.domain.features.diary.home.model.CalculatorFlow
import com.elta.android.domain.features.user.interactor.round
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseWidgetModel
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBarWidgetModel
import com.elta.android.presentation.core.compose.widgets.buttons.DownButtonClick
import com.elta.android.presentation.core.compose.widgets.buttons.DownButtonWidgetModel
import com.elta.android.presentation.core.compose.widgets.dialogs.BaseDialogWidgetModel
import com.elta.android.presentation.core.compose.widgets.textfields.SearchFieldAction
import com.elta.android.presentation.core.compose.widgets.textfields.SearchFieldWidgetModel
import com.elta.android.presentation.core.ui.fragment.DEBOUNCE_MILLIS
import com.elta.android.presentation.features.calcutator.mappers.ONE_DECIMAL_PLACE
import com.elta.android.presentation.features.calcutator.mappers.addFirstServing
import com.elta.android.presentation.features.calcutator.mappers.breadUnitsIsMax
import com.elta.android.presentation.features.calcutator.mappers.toDomain
import com.elta.android.presentation.features.calcutator.mappers.toUi
import com.elta.android.presentation.features.calcutator.products.model.CalculatorAction
import com.elta.android.presentation.features.calcutator.products.model.CalculatorViewState
import com.elta.android.presentation.features.calcutator.products.model.DishUiEntity
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

@FlowPreview
class CalculatorViewModel @Inject constructor(
    private val searchDishesInFatSecretUseCase: SearchDishesInFatSecretUseCase,
    private val searchProducts: SearchProductUseCase,
    private val getHistoryList: GetHistoryListUseCase,
    private val saveWordToHistory: SaveWordToHistoryUseCase,
    private val getCachedDishes: GetCachedDishesUseCase,
    private val addDishFragmentResult: AddDishFragmentResultHandler,
    private val calculatorFragmentResult: CalculatorFragmentResultHandler
) : BaseViewModel<CalculatorViewState>() {
    override fun createInitState(): CalculatorViewState =
        CalculatorViewState(
            dishes = emptyList(),
            startDishes = emptyList(),
            totalBreadUnits = 0.0,
            searchInFocus = false,
            lastWords = emptyList(),
            calculatorFlow = CalculatorFlow.BREAD_UNITS,
            isLoading = false,
            isError = false
        )

    private var _findingDishesState: MutableStateFlow<PagingData<DishUiEntity>> =
        MutableStateFlow(PagingData.empty())
    val findingDishesState: Flow<PagingData<DishUiEntity>> get() = _findingDishesState

    val appTopBar = BaseAppTopBarWidgetModel()
    val searchField = SearchFieldWidgetModel()
    val downButton = DownButtonWidgetModel()
    val dishDeleteConfirmDialog = BaseDialogWidgetModel<DishUiEntity>(
        positiveOnCLick = { deletedDish -> deletedDish?.let { deleteDish(it) } }
    )
    val clearProductsConfirmDialog = BaseDialogWidgetModel<Unit>(
        positiveOnCLick = { setDishes(emptyList()) }
    )
    val warningMaxBreadUnitsDialog = BaseDialogWidgetModel<Nothing>()
    val exitDialog = BaseDialogWidgetModel<Nothing>(
        positiveOnCLick = { router.exit() }
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

        launch {
            getCachedDishes()
                .catch { handleError(it) }
                .onEach { dishes ->
                    val dishesUi = dishes.toUi()
                    reduceState {
                        state.value.copy(
                            dishes = dishesUi,
                            startDishes = dishesUi
                        )
                    }
                    setDownButtonVisibility()
                }
        }

        launch {
            searchField.state
                .map { it.textField.text }
                .debounce(DEBOUNCE_MILLIS)
                .collect { searchText ->
                    if (searchText.isNotEmpty()) {
                        findDishes(searchText, state.value.calculatorFlow)
                    } else {
                        clearFindingDishes()
                    }
                }
        }

        launch {
            getCachedDishes()
                .map { it.toUi() }
                .collect {
                    reduceState {
                        state.value.copy(dishes = it, startDishes = it)
                    }
                    setDownButtonVisibility()
                }
        }
    }

    override val widgets: List<BaseWidgetModel<*>> = listOf(
        appTopBar,
        searchField,
        downButton
    ).actionObserve()

    override fun handleUserAction(action: Action) {
        when (action) {
            is CalculatorAction.LastWordClick -> searchField.setTextAndCursorToEnd(action.word)
            is CalculatorAction.DishClicked -> dishClick(action.dish)
            is CalculatorAction.CustomProductClicked -> customDishesClick()
            is CalculatorAction.CreateCustomProductClicked -> createDishClick()
            is CalculatorAction.DeleteDishClicked -> dishDeleteConfirmDialog.dialogOpen(action.dish)
            is CalculatorAction.ClearList -> clearProductsConfirmDialog.dialogOpen()
            is AppAction.BackPressure -> backClick()
            DownButtonClick -> saveDishes()
        }
    }

    override fun reduceStateByAction(
        currentState: CalculatorViewState,
        action: Action
    ): CalculatorViewState =
        when (action) {
            is SearchFieldAction.FocusChanged -> {
                val inFocusState = action.focusState.isFocused
                val isSearchEmpty = searchField.state.value.textField.text.isEmpty()
                downButton.visibilityState(!inFocusState && isSearchEmpty)
                currentState.copy(searchInFocus = inFocusState)
            }

            else -> currentState
        }

    override fun backClick() {
        when {
            state.value.searchInFocus -> reduceState { state.value.copy(searchInFocus = false) }
            state.value.isChanging() -> exitDialog.dialogOpen()
            else -> super.backClick()
        }
    }

    private fun setDishes(dishes: List<DishUiEntity>) {
        reduceState {
            val breadUnits = dishes.mapNotNull { it.breadUnits?.toDoubleOrNull() }
            state.value.copy(
                dishes = dishes,
                totalBreadUnits = if (breadUnits.isEmpty()) null else breadUnits.sumOf { it }
                    .round(ONE_DECIMAL_PLACE)
            )
        }
        clearFindingDishes()
        setDownButtonVisibility()
    }

    private fun setDownButtonVisibility() {
        downButton.setEnableState(state.value.isChanging())
    }

    private fun deleteDish(dish: DishUiEntity) {
        val newDishes = state.value.dishes
            .toMutableList()
            .apply { remove(dish) }
        setDishes(newDishes)
    }

    private fun editDishes(dish: DishUiEntity) {
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

        val breadIsMax = if (state.value.calculatorFlow == CalculatorFlow.BREAD_UNITS) {
            state.value.totalBreadUnits.breadUnitsIsMax()
        } else {
            false
        }


        if (breadIsMax) {
            warningMaxBreadUnitsDialog.dialogOpen()
        } else {
            launch {
                calculatorFragmentResult.onNext(state.value.dishes.toDomain())
                    .catch { handleError(it) }
                    .collect { router.exit() }
            }
        }
    }

    private fun dishClick(dish: DishUiEntity) {
        launch {
            saveWordToHistory(dish.name)
            getHistoryList()
                .catch { handleError(it) }
                .collectLatest {
                    reduceState { state.value.copy(lastWords = it) }
                    router.navigateTo(Screens.AddDishScreen(dish, state.value.calculatorFlow))
                }
        }
    }

    private fun customDishesClick() {
        router.navigateTo(Screens.CustomProductsScreen(state.value.calculatorFlow))
    }

    private fun createDishClick() {
        router.navigateTo(
            Screens.CreateCustomProductScreen(
                productName = searchField.state.value.textField.text,
                calculatorFlow = state.value.calculatorFlow
            )
        )
    }

    private fun clearFindingDishes() {
        _findingDishesState.tryEmit(PagingData.empty())
        searchField.clear()
    }

    private fun findDishes(name: String, calculatorFlow: CalculatorFlow) {
        launch {
            searchProducts(name = name, calculatorFlow = calculatorFlow)
                .catch {
                    handleError(it)
                    if (it is ServiceUnavailableError) {
                        reduceState { state.value.copy(isError = true, isLoading = false) }
                    }
                }
                .onStart { reduceState { state.value.copy(isLoading = true, isError = false) } }
                .onCompletion { reduceState { state.value.copy(isLoading = false) } }
                .map { pagingData -> pagingData.map { dish -> dish.toUi() } }
                .map { pagingData -> pagingData.map { dish -> dish.addFirstServing() } }
                .cachedIn(viewModelScope)
                .collect {
                    _findingDishesState.value = it
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
}
