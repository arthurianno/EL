package com.elta.android.presentation.features.calcutator.custom.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.elta.android.domain.features.calculator.interactor.DeleteProductUseCase
import com.elta.android.domain.features.calculator.interactor.SearchProductUseCase
import com.elta.android.presentation.Events
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseWidgetModel
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBarWidgetModel
import com.elta.android.presentation.core.compose.widgets.dialogs.BaseDialogWidgetModel
import com.elta.android.presentation.core.compose.widgets.textfields.SearchFieldWidgetModel
import com.elta.android.presentation.core.compose.widgets.textfields.SearchFieldAction
import com.elta.android.presentation.core.ui.fragment.DEBOUNCE_MILLIS
import com.elta.android.presentation.features.calcutator.custom.model.CustomProductAction
import com.elta.android.presentation.features.calcutator.custom.model.CustomProductsViewState
import com.elta.android.presentation.features.calcutator.mappers.toUi
import com.elta.android.presentation.features.calcutator.products.model.DishUiEntity
import com.nullgr.core.rx.RxBus
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

@FlowPreview
class CustomProductsViewModel @Inject constructor(
    private val bus: RxBus,
    private val searchProducts: SearchProductUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
) : BaseViewModel<CustomProductsViewState>() {

    val appTopBar = BaseAppTopBarWidgetModel()
    val searchField = SearchFieldWidgetModel()

    val confirmDialog = BaseDialogWidgetModel<DishUiEntity>(
        positiveOnCLick = { dish ->
            dish?.let {
                deleteProduct(dish)
            }
        }
    )

    private fun deleteProduct(dish: DishUiEntity) {
        launch {
            try {
                deleteProductUseCase(dish.id)
                findProducts(searchField.state.value.textField.text)
            } catch (ex: Exception) {
                handleError(ex)
                bus.event(Events.Sync.Server.ErrorWithMessage)
            }
        }
    }

    private val _customProductsState: MutableStateFlow<PagingData<DishUiEntity>> =
        MutableStateFlow(PagingData.empty())
    val customProductsState: Flow<PagingData<DishUiEntity>> get() = _customProductsState

    override fun createInitState(): CustomProductsViewState {
        return CustomProductsViewState(
            searchInFocus = false,
            isError = false
        )
    }

    init {
        findProducts()

        launch {
            searchField.state
                .map { it.textField.text }
                .distinctUntilChanged()
                .debounce(DEBOUNCE_MILLIS)
                .collectLatest {
                    if (it.isNotEmpty()) {
                        findProducts(it)
                    }
                }
        }
    }

    override val widgets: List<BaseWidgetModel<*>> = listOf(
        appTopBar,
        searchField
    ).actionObserve()

    override fun handleUserAction(action: Action) {
        when (action) {
            AppAction.BackPressure -> router.exit()
            is CustomProductAction.DeleteProductClicked -> confirmDialog.dialogOpen(action.dish)
            is CustomProductAction.ProductClicked -> router.navigateTo(Screens.CreateCustomProductScreen(action.dish))
            is CustomProductAction.CreateProduct -> router.navigateTo(Screens.CreateCustomProductScreen())
            is SearchFieldAction.FocusChanged -> reduceState { state.value.copy(searchInFocus = action.focusState.isFocused) }
        }
    }

    override fun reduceStateByAction(
        currentState: CustomProductsViewState,
        action: Action
    ): CustomProductsViewState =
        when (action) {
            CustomProductAction.ErrorResult -> state.value.copy(searchInFocus = false)
            else -> currentState
        }

    private fun findProducts(name: String = "") {
        launch {
            searchProducts(name, true)
                .catch {
                    handleError(it)
                    reduceState { state.value.copy(isError = true) }
                }
                .onStart { reduceState { state.value.copy(isError = false) } }
                .map { pagingData -> pagingData.map { dish -> dish.toUi() } }
                .cachedIn(viewModelScope)
                .collectLatest {
                    _customProductsState.value = it
                }
        }
    }

}
