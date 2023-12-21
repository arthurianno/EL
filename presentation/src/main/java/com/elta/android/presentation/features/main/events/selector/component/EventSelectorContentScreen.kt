package com.elta.android.presentation.features.main.events.selector.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.clickableWithNoRipple
import com.elta.android.presentation.core.compose.widgets.animation.VerticallyAnimation
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBar
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBarWidgetModel
import com.elta.android.presentation.core.compose.widgets.buttons.DownButton
import com.elta.android.presentation.core.compose.widgets.common.ErrorScreen
import com.elta.android.presentation.core.compose.widgets.common.LoadingScreen
import com.elta.android.presentation.core.compose.widgets.text.HelpText
import com.elta.android.presentation.core.compose.widgets.text.LastWords
import com.elta.android.presentation.core.compose.widgets.textfields.SearchField
import com.elta.android.presentation.features.main.events.selector.model.EventSelectorAction
import com.elta.android.presentation.features.main.events.selector.model.EventSelectorEvent
import com.elta.android.presentation.features.main.events.selector.model.EventSelectorViewState
import com.elta.android.presentation.features.main.events.selector.viewmodel.EventSelectorViewModel
import com.elta.android.presentation.theme.GetLocalProperties
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EventSelectorContentScreen(viewModel: EventSelectorViewModel) {
    val state = viewModel.state.collectAsState().value
    val searchState = viewModel.searchField.state.collectAsState().value
    val searchText = searchState.textField.text
    val keyboardController = LocalSoftwareKeyboardController.current
    val event = viewModel.event.collectAsState(initial = null).value
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(key1 = event) {
        when(event) {
            is EventSelectorEvent.ScrollToPosition -> {
                coroutineScope.launch {
                    val index = state.selection.indexOfFirst { it.id == event.id }
                    listState.animateScrollToItem(index, SCROLL_OFFSET)
                }
            }
            else -> Unit
        }
        viewModel.sendAction(EventSelectorAction.ClearEvents)
    }

    GetLocalProperties { dimens, _, colors, shapes, _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.gPurpurB)
                .clickableWithNoRipple {
                    keyboardController?.hide()
                }
        ) {
            Scaffold(
                scaffoldState = rememberScaffoldState(),
                topBar = { TopBar(viewModel.appTopBar, state.searchInFocus) },
                backgroundColor = colors.gPurpurB,
                modifier = Modifier
                    .statusBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = colors.white,
                            shape = if (state.searchInFocus) RectangleShape else shapes.sheet
                        )
                        .padding(it)
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = dimens.contentPadding,
                                top = dimens.contentPadding,
                                end = dimens.contentPadding,
                                bottom = dimens.halfMediumDim
                            )
                    ) {
                        SearchField(
                            widgetModel = viewModel.searchField,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                            searchInFocus = state.searchInFocus
                        )
                    }

                    val listIsNotEmpty = state.selection.any { item -> item.isVisible }

                    when {
                        searchState.isFocused && searchText.isBlank() -> {
                            LastSearchersBlock(state, searchText) { word ->
                                viewModel sendAction EventSelectorAction.LastSearchersClicked(word)
                            }
                        }

                        state.isLoading -> LoadingScreen(color = colors.shadeBlack1)
                        state.isError || !listIsNotEmpty -> ErrorScreen(
                            titleTextId = R.string.event_selector_medicine_not_found,
                            buttonTextId = R.string.add_text
                        ) {
                            viewModel sendAction EventSelectorAction.AddOther
                        }

                        else -> {
                            EventBlock(
                                value = state,
                                listState = listState,
                                isSearchTextEmpty = searchText.isEmpty(),
                                onSelectionClick = { id ->
                                    viewModel sendAction EventSelectorAction.SelectionClicked(id)
                                },
                                onRecentQueryClick = { data ->
                                    viewModel sendAction EventSelectorAction.RecentQueryClicked(data)
                                }
                            )
                        }
                    }
                }
            }
            if (!state.isLoading && !state.isError) {
                DownButton(widgetModel = viewModel.downButton)
            }
        }
    }
}

@Composable
private fun TopBar(
    appTopBarWidgetModel: BaseAppTopBarWidgetModel,
    searchInFocus: Boolean
) {
    GetLocalProperties { _, _, colors, _, types ->
        VerticallyAnimation(visualState = !searchInFocus, toUp = false) {
            BaseAppTopBar(
                widgetModel = appTopBarWidgetModel,
                backgroundColor = colors.gPurpurB,
                textStyle = types.h2,
                textColor = colors.white,
                startIcon = R.drawable.ic_back
            )
        }
    }
}

@Composable
private fun LastSearchersBlock(
    state: EventSelectorViewState,
    searchText: String,
    lastSearchersClick: (String) -> Unit
) {
    GetLocalProperties { dimens, _, _, _, _ ->
        Column(modifier = Modifier.padding(horizontal = dimens.contentPadding)) {
            VerticallyAnimation(visualState = state.lastSearchers.isNotEmpty()) {
                HelpText(searchText = searchText)
            }

            LastWords(lastWords = state.lastSearchers.map { it.name }) { word ->
                lastSearchersClick(word)
            }
        }
    }
}

private const val SCROLL_OFFSET = 1

@Preview
@Composable
private fun PreviewEventSelectorContentScreen() {
    EventSelectorContentScreen(viewModel())
}
