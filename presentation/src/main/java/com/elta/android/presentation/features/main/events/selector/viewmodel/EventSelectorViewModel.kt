package com.elta.android.presentation.features.main.events.selector.viewmodel

import com.elta.android.domain.features.diary.medicines.interactor.AddRecentlySearchesMedicamentsUseCase
import com.elta.android.domain.features.diary.medicines.interactor.GetMedicamentsUseCase
import com.elta.android.domain.features.diary.medicines.interactor.GetRecentlySearchesMedicamentsUseCase
import com.elta.android.domain.features.diary.medicines.interactor.GetRecentlyUsedMedicamentsUseCase
import com.elta.android.domain.features.diary.medicines.model.Medicament
import com.elta.android.presentation.Events
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseWidgetModel
import com.elta.android.presentation.core.compose.common.ClearEvent
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBarWidgetModel
import com.elta.android.presentation.core.compose.widgets.buttons.DownButtonClick
import com.elta.android.presentation.core.compose.widgets.buttons.DownButtonWidgetModel
import com.elta.android.presentation.core.compose.widgets.dialogs.BaseDialogWidgetModel
import com.elta.android.presentation.core.compose.widgets.textfields.SearchFieldAction
import com.elta.android.presentation.core.compose.widgets.textfields.SearchFieldWidgetModel
import com.elta.android.presentation.features.main.events.chooser.models.ChooserConfiguration
import com.elta.android.presentation.features.main.events.chooser.models.ChooserResult
import com.elta.android.presentation.features.main.events.selector.mapper.toUi
import com.elta.android.presentation.features.main.events.selector.model.EventSelectorAction
import com.elta.android.presentation.features.main.events.selector.model.EventSelectorEvent
import com.elta.android.presentation.features.main.events.selector.model.EventSelectorUi
import com.elta.android.presentation.features.main.events.selector.model.EventSelectorViewState
import com.nullgr.core.rx.RxBus
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class EventSelectorViewModel @Inject constructor(
    private val getRecentlyUsedMedicamentsUseCase: GetRecentlyUsedMedicamentsUseCase,
    private val getMedicamentsUseCase: GetMedicamentsUseCase,
    private val getRecentlySearchesMedicamentsUseCase: GetRecentlySearchesMedicamentsUseCase,
    private val addRecentlySearchesMedicamentsUseCase: AddRecentlySearchesMedicamentsUseCase,
    private val bus: RxBus,
) : BaseViewModel<EventSelectorViewState>() {

    val appTopBar = BaseAppTopBarWidgetModel()
    val searchField = SearchFieldWidgetModel()
    val downButton = DownButtonWidgetModel()

    val exitDialog = BaseDialogWidgetModel<Nothing>(positiveOnCLick = { router.exit() })

    override val widgets: List<BaseWidgetModel<*>> = listOf(
        appTopBar, downButton, searchField
    ).actionObserve()

    override fun createInitState(): EventSelectorViewState {
        return EventSelectorViewState(
            isLoading = true,
            isError = false,
            searchInFocus = false,
            lastSearchers = emptyList(),
            selection = emptyList(),
            recentlySelection = emptyList(),
            previousSelection = null
        )
    }

    init {

        launch {
            getRecentlyUsedMedicamentsUseCase()
                .onStart { reduceState { state.value.copy(isLoading = true) } }
                .catch { reduceState { state.value.copy(isError = true, isLoading = false) } }
                .map { medicamentInfo ->
                    medicamentInfo.map { (medicament, name) ->
                        medicament.toUi(state.value.previousSelection?.id, name)
                    }
                }
                .onCompletion {
                    reduceState {
                        state.value.copy(
                            isError = false, isLoading = false
                        )
                    }
                }.collect { list ->
                    reduceState {
                        state.value.copy(
                            recentlySelection = list
                        )
                    }
                }

        }

        launch {
            getMedicamentsUseCase()
                .onStart { reduceState { state.value.copy(isLoading = true) } }
                .catch { reduceState { state.value.copy(isError = true, isLoading = false) } }
                .map { it.toUi(state.value.previousSelection?.id) }.onCompletion {
                    reduceState {
                        state.value.copy(
                            isError = false, isLoading = false
                        )
                    }
                }.collectLatest { list ->
                    reduceState {
                        state.value.copy(
                            selection = list
                        )
                    }
                }
        }

        launch {
            searchField.state
                .map { it.textField.text }
                .distinctUntilChanged()
                .collectLatest {
                    searchMedicaments(it)
                }
        }

        launch {
            state.collect {
                val visibility = it.selection.any(EventSelectorUi::isSelected)
                downButton.visibilityState(visibility)
            }
        }

    }

    private fun searchMedicaments(name: String) {
        launch {
            reduceState {
                state.value.copy(
                    selection = state.value.selection.map { selection ->
                        selection.copy(
                            isVisible = selection.name.contains(other = name, ignoreCase = true)
                        )
                    },
                    recentlySelection = state.value.recentlySelection.map { selection ->
                        selection.copy(
                            isVisible = selection.name.contains(other = name, ignoreCase = true)
                        )
                    }
                )
            }
        }
    }

    override fun handleUserAction(action: Action) {
        when (action) {
            is EventSelectorAction.SelectionClicked -> updateSelection(action.id)
            is EventSelectorAction.RecentQueryClicked -> recentlyClick(
                action.data.first,
                action.data.second
            )

            is EventSelectorAction.AddOther -> addOtherSelection()
            is EventSelectorAction.LastSearchersClicked -> setSearchFieldText(action.word)
            is DownButtonClick -> confirm()
            is SearchFieldAction.FocusChanged -> focusChanged(action.focusState.isFocused)
            is EventSelectorAction.ClearEvents -> sendEvent(ClearEvent)
            is AppAction.BackPressure -> exit()
            else -> super.handleUserAction(action)
        }
    }

    private fun recentlyClick(id: Long, name: String) {
        reduceState {
            state.value.copy(
                recentlySelection = state.value.recentlySelection.map {
                    it.copy(isSelected = (it.id == id) && (it.name == name))
                },
                selection = state.value.selection.map { it.copy(isSelected = it.id == id) }
            )
        }
        sendEvent(EventSelectorEvent.ScrollToPosition(id))
    }

    private fun exit() {
        when (state.value.selection.find { it.isSelected }) {
            state.value.previousSelection -> router.exit()
            null -> router.exit()
            else -> exitDialog.dialogOpen()
        }
    }

    private fun setSearchFieldText(word: String) {
        searchField.setTextAndCursorToEnd(word)
    }

    private fun focusChanged(isFocused: Boolean) {
        reduceState {
            state.value.copy(searchInFocus = isFocused)
        }
        getLastSearches()
    }

    private fun addOtherSelection() {
        val list = state.value.selection.map { it.copy(isSelected = it.hasHint) }
        reduceState {
            state.value.copy(
                selection = list
            )
        }
        confirm()
    }

    private fun confirm(selectedId: Long? = null) {
        launch {
            val findItem = if (selectedId != null) {
                state.value.selection.find { it.id == selectedId }
            } else {
                state.value.selection.find { it.isSelected }
            }
            val otherName =
                state.value.recentlySelection.find { it.isSelected }?.name

            findItem
                ?.let { selectedItem ->
                    val medicament = selectedItem.meta
                    if (medicament is Medicament) {

                        if (searchField.state.value.textField.text.isNotBlank()) {
                            addRecentlySearchesMedicamentsUseCase(medicament)
                        }
                        val result = ChooserResult(
                            id = medicament.id.toString(),
                            name = medicament.name,
                            iconId = null,
                            meta = medicament to otherName
                        )
                        bus.event(Events.ChooserVariantSelected(result))
                        router.exit()
                    }

                }
        }
    }

    private fun updateSelection(id: Long) {
        reduceState {
            state.value.copy(
                selection = state.value.selection.map {
                    val selected = if (it.id == id)
                        !it.isSelected
                    else
                        false
                    it.copy(isSelected = selected)
                },
                recentlySelection = state.value.recentlySelection.map {
                    it.copy(isSelected = false)
                }
            )
        }
    }


    fun setConfiguration(chooserConfiguration: ChooserConfiguration) {
        chooserConfiguration.medicament?.let { medicamentChooser ->
            val medicament = Medicament(
                id = medicamentChooser.id,
                name = medicamentChooser.name,
                isDeleted = medicamentChooser.isDeleted,
                isOther = medicamentChooser.isOther,
                touchedAt = medicamentChooser.touchedAt
            )
            reduceState {
                state.value.copy(
                    previousSelection = EventSelectorUi(
                        id = medicament.id,
                        name = medicament.name,
                        hasHint = medicament.isOther,
                        isSelected = true,
                        isVisible = true,
                        meta = medicament
                    ),
                    selection = state.value.selection.map { it.copy(isSelected = it.id == medicament.id) })
            }
        }
    }

    private fun getLastSearches() {
        launch {
            getRecentlySearchesMedicamentsUseCase()
                .map { it.toUi() }
                .collect { lastSearchers ->
                    reduceState {
                        state.value.copy(
                            lastSearchers = lastSearchers
                        )
                    }
                }
        }
    }

}
