package com.elta.android.presentation.features.calcutator

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.fragment.app.viewModels
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.elta.android.domain.features.user.interactor.round
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.core.compose.common.NetworkState
import com.elta.android.presentation.core.compose.widgets.VSpacerMedium
import com.elta.android.presentation.core.compose.widgets.VSpacerSmall
import com.elta.android.presentation.core.compose.widgets.animation.VerticallyAnimation
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBar
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBarWidgetModel
import com.elta.android.presentation.core.compose.widgets.buttons.ButtonCircle
import com.elta.android.presentation.core.compose.widgets.buttons.DownButton
import com.elta.android.presentation.core.compose.widgets.dialogs.BaseDialog
import com.elta.android.presentation.core.compose.widgets.text.BreadUnitsLabel
import com.elta.android.presentation.core.compose.widgets.textfields.SearchField
import com.elta.android.presentation.features.calcutator.component.CardBody
import com.elta.android.presentation.features.calcutator.component.FindingDishes
import com.elta.android.presentation.features.calcutator.model.CalculatorAction
import com.elta.android.presentation.features.calcutator.model.CalculatorViewState
import com.elta.android.presentation.features.calcutator.model.DishUiEntity
import com.elta.android.presentation.features.calcutator.viewmodel.CalculatorViewModel
import com.elta.android.presentation.features.calcutator.viewmodel.ONE_DECIMAL_PLACE
import com.elta.android.presentation.theme.GetLocalProperties
import com.elta.android.presentation.theme.LocalNetworkState
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class)
class CalculatorFragment : BaseComposeFragment<CalculatorViewModel>() {

    override val viewModel: CalculatorViewModel by viewModels { viewModelFactory }

    override fun CalculatorViewModel.init() {
        appTopBar.setTitle(getString(R.string.calculator_appbar_title))
        appTopBar.setStartIconAction(AppAction.BackPressure)
        searchField.setHint(getString(R.string.calculator_search_hint))
        downButton.setText(getString(R.string.calculator_save_text))
        setHelpText(getString(R.string.calculator_help_text_add_dishes))
        dishDeleteConfirmDialog.initDialog(
            message = getString(R.string.calculator_dish_delete_request),
            positiveButtonText = getString(R.string.yes_text),
            negativeButtonText = getString(R.string.no_text)
        )
        warningMaxBreadUnitsDialog.initDialog(
            title = getString(R.string.calculator_dialog_title_warning),
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
        BaseDialog(widgetModel = viewModel.warningMaxBreadUnitsDialog)
        BaseDialog(widgetModel = viewModel.exitDialog)
    }

    @Composable
    override fun Content(viewModel: CalculatorViewModel) {
        val state = viewModel.state.collectAsState().value

        val findingDishesState = viewModel.findingPager.collectAsLazyPagingItems()

        val searchState = viewModel.searchField.state.collectAsState().value
        val dishes = state.dishes

        val searchText = searchState.textField.text
        val searchInFocus = state.searchInFocus

        val networkAvailable = LocalNetworkState.current == NetworkState.Available

        GetLocalProperties { dimens, _, colors, shapes, _ ->
            val systemBarColor = animateColorAsState(
                targetValue = if (searchInFocus) colors.shadeBlack3 else colors.gOrangeB,
                label = ""
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = systemBarColor.value)
            ) {
                Scaffold(
                    scaffoldState = rememberScaffoldState(),
                    topBar = { CalculatorTopBar(viewModel.appTopBar, searchInFocus) },
                    bottomBar = { DownButton(widgetModel = viewModel.downButton) },
                    backgroundColor = colors.gOrangeB,
                    modifier = Modifier.statusBarsPadding()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(it)
                            .background(
                                color = colors.white,
                                shape = if (searchInFocus) RectangleShape else shapes.sheet
                            )
                            .padding(dimens.contentPadding)
                    ) {
                        if (networkAvailable) {
                            SearchField(
                                widgetModel = viewModel.searchField,
                                searchInFocus = searchInFocus
                            )
                        }
                        VSpacerMedium()
                        HelpText(
                            state = state,
                            searchText = searchText,
                            searchInFocus
                        )
                        VSpacerSmall()
                        if (searchInFocus) {
                            SearchView(
                                state = state,
                                searchText = searchText,
                                viewModel = viewModel,
                                findingDishesState = findingDishesState
                            )
                        } else {
                            MainContent(
                                dishes = dishes,
                                networkAvailable = networkAvailable,
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun MainContent(
        dishes: List<DishUiEntity>,
        networkAvailable: Boolean,
        viewModel: CalculatorViewModel
    ) {
        if (dishes.isEmpty()) {
            EmptyContent()
        } else {
            CalculateDishes(
                dishes = dishes,
                networkAvailable = networkAvailable,
                viewModel = viewModel
            )
        }
    }

    @Composable
    private fun SearchView(
        state: CalculatorViewState,
        searchText: String,
        viewModel: CalculatorViewModel,
        findingDishesState: LazyPagingItems<DishUiEntity>
    ) {
        if (searchText.isEmpty()) {
            LastWords(
                lastWords = state.lastWords,
                viewModel = viewModel
            )
        } else {
            FindingDishes(findingDishes = findingDishesState, resources) { dish ->
                dish?.let { viewModel sendAction CalculatorAction.DishClick(dish) }
            }
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
                    Text(
                        text = word,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel sendAction CalculatorAction.LastWordClick(word)
                                keyboardController?.hide()
                            }
                            .padding(vertical = dimens.lastWordVertical)
                    )
                }
            }
        }
    }

    @Composable
    private fun CalculateDishes(
        dishes: List<DishUiEntity>,
        networkAvailable: Boolean,
        viewModel: CalculatorViewModel
    ) {
        val totalCountBreadUnits =
            dishes.sumOf { it.breadUnits.toDouble() }.round(ONE_DECIMAL_PLACE).toString()
        TotalCountBreadUnits(totalCountBreadUnits)
        VSpacerSmall()
        SelectedDishes(
            dishes = dishes,
            networkAvailable = networkAvailable,
            viewModel = viewModel
        )
    }

    @Composable
    private fun SelectedDishes(
        dishes: List<DishUiEntity>,
        networkAvailable: Boolean,
        viewModel: CalculatorViewModel
    ) {
        GetLocalProperties { dimens, _, colors, shapes, _ ->
            LazyColumn(verticalArrangement = Arrangement.spacedBy(dimens.smallDim)) {
                items(items = dishes) { dish ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                            .clip(shape = shapes.dishCard)
                            .clickable(enabled = networkAvailable, onClick = {
                                viewModel sendAction CalculatorAction.DishClick(dish)
                            })
                            .border(
                                width = dimens.borderWidth,
                                color = colors.shadeBlack3,
                                shape = shapes.dishCard
                            )
                            .padding(
                                horizontal = dimens.contentPadding,
                                vertical = dimens.halfMediumDim
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CardBody(dish)
                        Column(
                            Modifier.fillMaxHeight(),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.End
                        ) {
                            if (networkAvailable) {
                                CloseButton(dish = dish, viewModel = viewModel)
                            }
                            BreadUnitsLabel(breadUnitsCount = dish.breadUnits)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun CloseButton(
        dish: DishUiEntity,
        viewModel: CalculatorViewModel
    ) {
        ButtonCircle(
            icon = R.drawable.btn_close,
            onClick = {
                viewModel sendAction CalculatorAction.DeleteDishClick(dish)
            },
            contentDescriptionId = R.string.content_description_close_button
        )
    }

    @Composable
    private fun TotalCountBreadUnits(totalCountBreadUnits: String) {
        GetLocalProperties { dimens, _, colors, shapes, _ ->
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
                Text(text = stringResource(id = R.string.calculator_total_bread_units))
                BreadUnitsLabel(totalCountBreadUnits)
            }
        }
    }

    @Composable
    private fun CalculatorTopBar(
        appTopBarWidgetModel: BaseAppTopBarWidgetModel,
        searchInFocus: Boolean
    ) {
        GetLocalProperties { _, _, colors, _, types ->
            VerticallyAnimation(visualState = !searchInFocus, toUp = false) {
                BaseAppTopBar(
                    widgetModel = appTopBarWidgetModel,
                    backgroundColor = colors.gOrangeB,
                    textStyle = types.h2,
                    textColor = colors.white,
                    startIcon = R.drawable.ic_back
                )
            }
        }
    }

    @Composable
    private fun HelpText(
        state: CalculatorViewState,
        searchText: String,
        searchInFocus: Boolean
    ) {
        GetLocalProperties { _, _, colors, _, _ ->
            Text(
                text = if (!searchInFocus) {
                    state.helpText
                } else {
                    stringResource(
                        id = if (searchText.isEmpty()) {
                            R.string.calculator_last_search
                        } else {
                            R.string.calculator_search_result
                        }
                    )
                },
                color = colors.shadeBlack2
            )
        }
    }

    @Composable
    private fun EmptyContent() {
        GetLocalProperties { dimens, _, colors, _, types ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
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
}
