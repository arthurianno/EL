package com.elta.android.presentation.features.calcutator.products

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.clickableWithNoRipple
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.core.compose.common.NetworkState
import com.elta.android.presentation.core.compose.widgets.HSpacerSmall
import com.elta.android.presentation.core.compose.widgets.VSpacerHalfMedium
import com.elta.android.presentation.core.compose.widgets.VSpacerMedium
import com.elta.android.presentation.core.compose.widgets.VSpacerSmall
import com.elta.android.presentation.core.compose.widgets.VSpacerVerySmall
import com.elta.android.presentation.core.compose.widgets.animation.VerticallyAnimation
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBar
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBarWidgetModel
import com.elta.android.presentation.core.compose.widgets.buttons.DownButton
import com.elta.android.presentation.core.compose.widgets.dialogs.BaseDialog
import com.elta.android.presentation.core.compose.widgets.text.BreadUnitsLabel
import com.elta.android.presentation.core.compose.widgets.textfields.SearchField
import com.elta.android.presentation.features.calcutator.component.DishesItem
import com.elta.android.presentation.features.calcutator.component.TrailingIcon
import com.elta.android.presentation.features.calcutator.custom.component.CustomProductButton
import com.elta.android.presentation.features.calcutator.mappers.format
import com.elta.android.presentation.features.calcutator.products.component.FindingDishes
import com.elta.android.presentation.features.calcutator.products.model.CalculatorAction
import com.elta.android.presentation.features.calcutator.products.model.DishUiEntity
import com.elta.android.presentation.features.calcutator.products.viewmodel.CalculatorViewModel
import com.elta.android.presentation.theme.GetLocalProperties
import com.elta.android.presentation.theme.LocalNetworkState
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class, ExperimentalComposeUiApi::class)
class CalculatorFragment : BaseComposeFragment<CalculatorViewModel>() {

    override val viewModel: CalculatorViewModel by viewModels { viewModelFactory }

    companion object {
        fun newInstance(): Fragment {
            return CalculatorFragment()
        }
    }

    override fun CalculatorViewModel.init() {
        appTopBar.setTitle(getString(R.string.calculator_appbar_title))
        appTopBar.setStartIconAction(AppAction.BackPressure)
        searchField.setHint(getString(R.string.calculator_search_hint_product))
        downButton.setText(getString(R.string.calculator_save_list))
        dishDeleteConfirmDialog.initDialog(
            message = getString(R.string.calculator_dish_delete_request),
            positiveButtonText = getString(R.string.yes_text),
            negativeButtonText = getString(R.string.no_text)
        )
        clearProductsConfirmDialog.initDialog(
            title = getString(R.string.calculator_clear_list_title),
            message = getString(R.string.calculator_clear_list_message),
            positiveButtonText = getString(R.string.yes_text),
            negativeButtonText = getString(R.string.no_text)
        )
        warningMaxBreadUnitsDialog.initDialog(
            title = getString(R.string.calculator_max_bread_units_title),
            message = getString(R.string.calculator_max_bread_units_message),
            positiveButtonText = getString(R.string.ok)
        )
        exitDialog.initDialog(
            title = getString(R.string.event_form_dialog_title),
            message = getString(R.string.event_form_exit_dialog_body),
            positiveButtonText = getString(R.string.event_form_exit_dialog_confirm_button),
            negativeButtonText = getString(R.string.event_form_dialog_cancel_button)
        )
    }

    @Composable
    override fun Dialogs(viewModel: CalculatorViewModel) {
        BaseDialog(widgetModel = viewModel.dishDeleteConfirmDialog)
        BaseDialog(widgetModel = viewModel.clearProductsConfirmDialog)
        BaseDialog(widgetModel = viewModel.warningMaxBreadUnitsDialog)
        BaseDialog(widgetModel = viewModel.exitDialog)
    }

    @Composable
    override fun Content(viewModel: CalculatorViewModel) {
        val state = viewModel.state.collectAsState().value

        val networkAvailable = LocalNetworkState.current == NetworkState.Available

        val keyboardController = LocalSoftwareKeyboardController.current
        val focusManager = LocalFocusManager.current

        if (!state.searchInFocus) {
            focusManager.clearFocus()
        }
        GetLocalProperties { dimens, _, colors, shapes, _ ->
            val systemBarColor = animateColorAsState(
                targetValue = if (state.searchInFocus) colors.shadeBlack3 else colors.gOrangeA,
                label = ""
            )
            Box(modifier = Modifier
                .fillMaxSize()
                .background(color = systemBarColor.value)
                .clickableWithNoRipple {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }) {
                Scaffold(
                    scaffoldState = rememberScaffoldState(),
                    topBar = { CalculatorTopBar(viewModel.appTopBar, state.searchInFocus) },
                    bottomBar = { DownButton(widgetModel = viewModel.downButton) },
                    backgroundColor = colors.gOrangeA,
                    modifier = Modifier.statusBarsPadding()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(it)
                            .background(
                                color = colors.white,
                                shape = if (state.searchInFocus) RectangleShape else shapes.sheet
                            )
                            .padding(
                                start = dimens.contentPadding,
                                top = dimens.contentPadding,
                                end = dimens.contentPadding
                            )
                    ) {
                        if (networkAvailable) {
                            SearchField(
                                widgetModel = viewModel.searchField,
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                                searchInFocus = state.searchInFocus
                            )
                        }
                        MainBlock(viewModel, focusManager)
                    }
                }
            }
        }
    }

    @Composable
    fun MainBlock(viewModel: CalculatorViewModel, focusManager: FocusManager) {

        val state = viewModel.state.collectAsState().value
        val findingDishesState = viewModel.findingDishesState.collectAsLazyPagingItems()
        val searchState = viewModel.searchField.state.collectAsState().value
        val searchText = searchState.textField.text

        val listState = rememberLazyListState()

        CustomProductsBlockInSearch(
            customProductsClicked = { viewModel.sendAction(CalculatorAction.CustomProductClicked) },
            createCustomProductClicked = { viewModel.sendAction(CalculatorAction.CreateCustomProductClicked) },
            menuVisibility = listState.isScrollingUp(),
        )

        VSpacerMedium()

        if (searchText.isNotBlank() || state.searchInFocus) {
            HelpText(searchText)
        }

        if (findingDishesState.itemCount != 0 || state.searchInFocus) {
            VSpacerSmall()

            SearchView(
                lastWords = state.lastWords,
                searchText = searchText,
                viewModel = viewModel,
                findingDishesState = findingDishesState,
                focusManager = focusManager,
                listState = listState
            )
        } else {
            MainContent(
                dishes = state.dishes, viewModel = viewModel, listState = listState
            )
        }
    }

    @Composable
    private fun CustomProductsBlockInSearch(
        customProductsClicked: () -> Unit, createCustomProductClicked: () -> Unit,
        menuVisibility: Boolean
    ) {
        AnimatedVisibility(
            visible = menuVisibility,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Column {
                VSpacerMedium()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CustomProductButton(
                        textId = R.string.custom_products_my_products,
                        iconId = R.drawable.ic_list,
                        callback = customProductsClicked
                    )
                    HSpacerSmall()
                    CustomProductButton(
                        textId = R.string.custom_products_create_product,
                        iconId = R.drawable.ic_home_plus,
                        callback = createCustomProductClicked
                    )
                }
            }
        }

    }

    @Composable
    private fun MainContent(
        dishes: List<DishUiEntity>,
        viewModel: CalculatorViewModel,
        listState: LazyListState
    ) {
        if (dishes.isEmpty()) {
            EmptyContent()
        } else {
            CalculateDishes(
                dishes = dishes,
                viewModel = viewModel,
                listState = listState
            )
        }
    }

    @Composable
    private fun SearchView(
        lastWords: List<String>,
        searchText: String,
        viewModel: CalculatorViewModel,
        findingDishesState: LazyPagingItems<DishUiEntity>,
        focusManager: FocusManager,
        listState: LazyListState,
    ) {
        if (searchText.isEmpty()) {
            LastWords(
                lastWords = lastWords, viewModel = viewModel
            )
        } else {
            FindingDishes(
                dishes = findingDishesState,
                trailingIcon = TrailingIcon.AddDish,
                dishesClick = { dish ->
                    focusManager.clearFocus()
                    viewModel sendAction CalculatorAction.DishClicked(dish)
                },
                deleteClick = { dish ->
                    viewModel sendAction CalculatorAction.DeleteDishClicked(dish)
                },
                listState = listState
            )
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    private fun LastWords(
        lastWords: List<String>,
        viewModel: CalculatorViewModel
    ) {
        GetLocalProperties { dimens, _, _, _, _ ->
            val keyboardController = LocalSoftwareKeyboardController.current
            LazyColumn {
                items(items = lastWords) { word ->
                    Text(text = word, modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel sendAction CalculatorAction.LastWordClick(word)
                            keyboardController?.hide()
                        }
                        .padding(vertical = dimens.lastWordVertical))
                }
            }
        }
    }

    @Composable
    private fun CalculateDishes(
        dishes: List<DishUiEntity>, viewModel: CalculatorViewModel,
        listState: LazyListState
    ) {

        val totalCountBreadUnits = dishes.sumOf { it.breadUnits.toDouble() }.format()
        TotalCountBreadUnits(totalCountBreadUnits, dishes.count()) {
            viewModel.sendAction(
                CalculatorAction.ClearList
            )
        }
        VSpacerHalfMedium()
        SelectedDishes(
            dishes = dishes,
            viewModel = viewModel,
            listState = listState
        )
    }

    @Composable
    private fun SelectedDishes(
        dishes: List<DishUiEntity>, viewModel: CalculatorViewModel,
        listState: LazyListState
    ) {
        GetLocalProperties { dimens, _, _, _, _ ->
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(dimens.smallDim),
                state = listState
            ) {
                items(items = dishes) { dish ->
                    DishesItem(
                        dish = dish,
                        trailingIcon = TrailingIcon.BreadUnit,
                        dishesClick = {
                            viewModel.sendAction(CalculatorAction.DishClicked(it))
                        },
                        deleteClick = {
                            viewModel.sendAction(CalculatorAction.DeleteDishClicked(it))
                        })
                }
                item { VSpacerVerySmall() }
            }
        }
    }

    @Composable
    private fun TotalCountBreadUnits(
        totalCountBreadUnits: String, count: Int, clearListClicked: () -> Unit
    ) {
        GetLocalProperties { dimens, _, colors, shapes, types ->
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = dimens.halfMediumDim),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(id = R.string.calculator_total_products_title),
                        style = types.body1,
                        color = colors.shadeBlack2
                    )
                    Text(text = stringResource(id = R.string.calculator_total_products_clear_list),
                        style = types.body1,
                        color = colors.gOrangeB,
                        modifier = Modifier.clickableWithNoRipple { clearListClicked() })
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = dimens.borderWidth,
                            color = colors.shadeBlack3,
                            shape = shapes.dishCard
                        )
                        .padding(dimens.halfMediumDim),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = pluralStringResource(
                            id = R.plurals.calculator_total_products, count = count, count
                        )
                    )
                    BreadUnitsLabel(totalCountBreadUnits)
                }
            }

        }
    }

    @Composable
    private fun CalculatorTopBar(
        appTopBarWidgetModel: BaseAppTopBarWidgetModel, searchInFocus: Boolean
    ) {
        GetLocalProperties { _, _, colors, _, types ->
            VerticallyAnimation(visualState = !searchInFocus, toUp = false) {
                BaseAppTopBar(
                    widgetModel = appTopBarWidgetModel,
                    backgroundColor = colors.gOrangeA,
                    textStyle = types.h2,
                    textColor = colors.white,
                    startIcon = R.drawable.ic_back
                )
            }
        }
    }

    @Composable
    private fun HelpText(searchText: String) {
        GetLocalProperties { _, _, colors, _, _ ->

            val textId = if (searchText.isNotBlank()) {
                R.string.calculator_search_result
            } else {
                R.string.calculator_last_search
            }

            Text(
                text = stringResource(textId), color = colors.shadeBlack2
            )

            VSpacerSmall()
        }
    }

    @Composable
    private fun EmptyContent() {
        GetLocalProperties { dimens, _, colors, _, types ->
            Box(
                contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = stringResource(id = R.string.calculator_empty_list_text),
                    style = types.body1.copy(color = colors.shadeBlack2),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(dimens.contentPadding)
                )
            }
        }
    }

    @Composable
    private fun LazyListState.isScrollingUp(): Boolean {
        var previousIndex by remember(this) { mutableStateOf(firstVisibleItemIndex) }
        var previousScrollOffset by remember(this) { mutableStateOf(firstVisibleItemScrollOffset) }
        return remember(this) {
            derivedStateOf {
                when {
                    firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0 -> true
                    layoutInfo.visibleItemsInfo.lastOrNull()?.index == layoutInfo.totalItemsCount - 1 -> false
                    previousIndex != firstVisibleItemIndex -> previousIndex > firstVisibleItemIndex
                    else -> previousScrollOffset > firstVisibleItemScrollOffset
                }.also {
                    previousIndex = firstVisibleItemIndex
                    previousScrollOffset = firstVisibleItemScrollOffset
                }
            }
        }.value
    }

    @Preview
    @Composable
    private fun PreviewCalculatorFragment() {
        Column {
            MainBlock(viewModel = viewModel(), LocalFocusManager.current)
        }
    }
}