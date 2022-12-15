package com.elta.android.presentation.features.calcutator

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.fragment.app.viewModels
import com.elta.android.domain.features.user.interactor.round
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.core.compose.common.NetworkState
import com.elta.android.presentation.core.compose.widgets.VSpacerMedium
import com.elta.android.presentation.core.compose.widgets.VSpacerSmall
import com.elta.android.presentation.core.compose.widgets.VSpacerVerySmall
import com.elta.android.presentation.core.compose.widgets.animation.VerticallyAnimation
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBar
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBarWidgetModel
import com.elta.android.presentation.core.compose.widgets.buttons.ButtonCircle
import com.elta.android.presentation.core.compose.widgets.buttons.DownButton
import com.elta.android.presentation.core.compose.widgets.dialogs.BaseDialog
import com.elta.android.presentation.core.compose.widgets.text.BreadUnitsLabel
import com.elta.android.presentation.core.compose.widgets.textfields.SearchField
import com.elta.android.presentation.features.calcutator.model.CalculatorViewState
import com.elta.android.presentation.features.calcutator.model.DishUiEntity
import com.elta.android.presentation.features.calcutator.viewmodel.CalculatorViewModel
import com.elta.android.presentation.theme.GetLocalProperties
import com.elta.android.presentation.theme.LocalNetworkState

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
    override fun Dialogs() {
        BaseDialog(widgetModel = viewModel.dishDeleteConfirmDialog)
        BaseDialog(widgetModel = viewModel.warningMaxBreadUnitsDialog)
        BaseDialog(widgetModel = viewModel.exitDialog)
    }

    @Composable
    override fun Content(viewModel: CalculatorViewModel) {
        val state = viewModel.state.collectAsState()
        val dishes = state.value.dishes
        val searchInFocus = viewModel.state.collectAsState().value.searchInFocus
        val networkAvailable = LocalNetworkState.current == NetworkState.Available
        GetLocalProperties { dimens, _, colors, shapes, _ ->
            val systemBarColor = animateColorAsState(
                targetValue = if (searchInFocus) colors.shadeBlack3 else colors.gOrangeB
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = systemBarColor.value)
            ) {
                Scaffold(
                    scaffoldState = rememberScaffoldState(),
                    topBar = { CalculatorTopBar(viewModel.appTopBar, searchInFocus) },
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
                            .padding(bottom = dimens.downButtonHeight)
                            .padding(dimens.contentPadding)
                    ) {
                        if (networkAvailable) {
                            SearchField(
                                widgetModel = viewModel.searchField,
                                searchInFocus = searchInFocus
                            )
                        }
                        VSpacerMedium()
                        HelpText(viewModel, searchInFocus)
                        VSpacerSmall()
                        if (searchInFocus) {
                            SearchView(viewModel, state)
                        } else {
                            MainContent(
                                viewModel = viewModel,
                                dishes = dishes,
                                networkAvailable = networkAvailable
                            )
                        }
                    }
                }
                DownButton(widgetModel = viewModel.downButton)
            }
        }
    }

    @Composable
    private fun MainContent(
        viewModel: CalculatorViewModel,
        dishes: List<DishUiEntity>,
        networkAvailable: Boolean
    ) {
        if (dishes.isEmpty()) {
            EmptyContent()
        } else {
            CalculateDishes(
                dishes = dishes,
                viewModel = viewModel,
                networkAvailable = networkAvailable
            )
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    private fun SearchView(
        viewModel: CalculatorViewModel,
        state: State<CalculatorViewState>
    ) {
        val searchFieldState = viewModel.searchField.state.collectAsState()
        val keyboardController = LocalSoftwareKeyboardController.current
        if (searchFieldState.value.textField.text.isEmpty()) {
            LastWords(
                lastWords = state.value.lastWords,
                onClick = {
                    viewModel.lastWordOnClick(it)
                    keyboardController?.hide()
                }
            )
        } else {
            FindingDishes(
                findingDishes = state.value.findingDishes,
                isFindDishes = state.value.isFindDishes,
                onClick = viewModel::dishOnClick
            )
        }
    }

    @Composable
    private fun FindingDishes(
        findingDishes: List<DishUiEntity>,
        isFindDishes: Boolean,
        onClick: (dish: DishUiEntity) -> Unit
    ) {
        GetLocalProperties { dimens, _, colors, shapes, _ ->
            Box(modifier = Modifier.fillMaxSize()) {
                if (isFindDishes) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    if (findingDishes.isEmpty()) {
                        Text(
                            text = stringResource(id = R.string.calculator_search_no_result),
                            color = colors.shadeBlack2,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(dimens.dishCardVerticalSpace)) {
                            items(items = findingDishes) { dish ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(shapes.dishCard)
                                        .clickable { onClick(dish) }
                                        .border(
                                            dimens.borderWidth,
                                            colors.shadeBlack3,
                                            shapes.dishCard
                                        )
                                        .padding(dimens.contentPadding),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CardBody(dish)
                                    Image(
                                        painter = painterResource(id = R.drawable.btn_plus),
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun RowScope.CardBody(dish: DishUiEntity) {
        GetLocalProperties { dimens, _, colors, _, types ->
            Column(
                modifier = Modifier
                    .padding(end = dimens.contentPadding)
                    .weight(1f)
            ) {
                Row {
                    if (dish.isVerification) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_verify_dish),
                            contentDescription = null
                        )
                    }
                    Text(
                        text = dish.name,
                        style = types.title3,
                        color = colors.blackBlue,
                        modifier = Modifier.padding(start = dimens.verySmallDim)
                    )
                }
                VSpacerVerySmall()
            }
        }
    }

    @Composable
    private fun LastWords(lastWords: List<String>, onClick: (word: String) -> Unit) {
        GetLocalProperties { dimens, _, _, _, _ ->
            LazyColumn() {
                items(items = lastWords) { word ->
                    Text(
                        text = word,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onClick(word) }
                            .padding(vertical = dimens.lastWordVertical)
                    )
                }
            }
        }
    }

    @Composable
    private fun CalculateDishes(
        dishes: List<DishUiEntity>,
        viewModel: CalculatorViewModel,
        networkAvailable: Boolean
    ) {
        val totalCountBreadUnits = dishes.sumOf { it.breadUnits }.round(1)
        TotalCountBreadUnits(totalCountBreadUnits)
        VSpacerSmall()
        SelectedDishes(
            dishes,
            networkAvailable,
            onCardClick = viewModel::dishOnClick,
            onCloseClick = viewModel::dishDeleteOnClick
        )
    }

    @Composable
    private fun SelectedDishes(
        dishes: List<DishUiEntity>,
        networkAvailable: Boolean,
        onCardClick: (dish: DishUiEntity) -> Unit,
        onCloseClick: (dish: DishUiEntity) -> Unit
    ) {
        GetLocalProperties { dimens, _, colors, shapes, _ ->
            LazyColumn(verticalArrangement = Arrangement.spacedBy(dimens.smallDim)) {
                items(items = dishes) { dish ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(shape = shapes.dishCard)
                            .clickable(enabled = networkAvailable, onClick = { onCardClick(dish) })
                            .border(
                                width = dimens.borderWidth,
                                color = colors.shadeBlack3,
                                shape = shapes.dishCard
                            )
                            .padding(dimens.contentPadding)
                    ) {
                        DishInfoBlock(dish)
                        if (networkAvailable) {
                            CloseButton(
                                dish = dish,
                                onCloseClick = onCloseClick
                            )
                        }
                        BreadUnitLabel(dish)
                    }
                }
            }
        }
    }

    @Composable
    private fun BoxScope.DishInfoBlock(
        dish: DishUiEntity
    ) {
        GetLocalProperties { dimens, _, colors, _, types ->
            Column(
                modifier = Modifier
                    .padding(end = dimens.dishCardTextEndPadding)
                    .align(Alignment.CenterStart),
                verticalArrangement = Arrangement.spacedBy(dimens.verySmallDim)
            ) {
                Text(text = dish.name, style = types.title3)
                Text(
                    text = "${dish.servingAmount.toInt()} ${dish.servingSelect.servingDescription}",
                    color = colors.shadeBlack1
                )
                Text(
                    text = stringResource(id = R.string.calculator_dish_card_in_port),
                    color = colors.shadeBlack1
                )
            }
        }
    }

    @Composable
    private fun BoxScope.BreadUnitLabel(dish: DishUiEntity) {
        Box(modifier = Modifier.align(Alignment.BottomEnd)) {
            BreadUnitsLabel(breadUnitsCount = dish.breadUnits)
        }
    }

    @Composable
    private fun BoxScope.CloseButton(
        dish: DishUiEntity,
        onCloseClick: (dish: DishUiEntity) -> Unit
    ) {
        Box(modifier = Modifier.align(Alignment.TopEnd)) {
            ButtonCircle(
                icon = R.drawable.btn_close,
                onClick = { onCloseClick(dish) }
            )
        }
    }

    @Composable
    private fun TotalCountBreadUnits(totalCountBreadUnits: Double) {
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
        viewModel: CalculatorViewModel,
        searchInFocus: Boolean
    ) {
        GetLocalProperties { _, _, colors, _, _ ->
            val state = viewModel.state.collectAsState()
            val searchFieldState = viewModel.searchField.state.collectAsState()
            Text(
                text = if (!searchInFocus) {
                    state.value.helpText
                } else {
                    stringResource(
                        id = if (searchFieldState.value.textField.text.isEmpty()) {
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
