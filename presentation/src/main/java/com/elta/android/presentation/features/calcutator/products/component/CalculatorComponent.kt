@file:OptIn(FlowPreview::class)

package com.elta.android.presentation.features.calcutator.products.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.elta.android.domain.features.diary.home.model.CalculatorFlow
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.clickableWithNoRipple
import com.elta.android.presentation.core.compose.widgets.HSpacerSmall
import com.elta.android.presentation.core.compose.widgets.VSpacerHalfMedium
import com.elta.android.presentation.core.compose.widgets.VSpacerMedium
import com.elta.android.presentation.core.compose.widgets.VSpacerSmall
import com.elta.android.presentation.core.compose.widgets.animation.VerticallyAnimation
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBar
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBarWidgetModel
import com.elta.android.presentation.core.compose.widgets.text.BreadUnitsLabel
import com.elta.android.presentation.core.compose.widgets.text.HelpText
import com.elta.android.presentation.features.calcutator.custom.component.CustomProductButton
import com.elta.android.presentation.features.calcutator.mappers.ZERO_COUNT_DOUBLE
import com.elta.android.presentation.features.calcutator.mappers.format
import com.elta.android.presentation.features.calcutator.products.model.CalculatorAction
import com.elta.android.presentation.features.calcutator.products.model.DishUiEntity
import com.elta.android.presentation.features.calcutator.products.viewmodel.CalculatorViewModel
import com.elta.android.presentation.theme.GetLocalProperties
import kotlinx.coroutines.FlowPreview

@Composable
internal fun CalculatorTopBar(
    appTopBarWidgetModel: BaseAppTopBarWidgetModel,
    searchInFocus: Boolean
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

@FlowPreview
@Composable
internal fun MainBlock(
    viewModel: CalculatorViewModel,
    focusManager: FocusManager = LocalFocusManager.current
) {
    val state = viewModel.state.collectAsState()
    val findingDishesState = viewModel.findingDishesState.collectAsLazyPagingItems()
    val searchState = viewModel.searchField.state.collectAsState()
    val searchText = searchState.value.textField.text
    val listState = rememberLazyListState()

    CustomProductsBlockInSearch(
        customProductsClicked = {
            viewModel sendAction CalculatorAction.CustomProductClicked
        },
        createCustomProductClicked = {
            viewModel sendAction CalculatorAction.CreateCustomProductClicked
        },
        menuVisibility = listState.isScrollingUp()
    )
    VSpacerMedium()

    if (searchText.isNotBlank() || state.value.searchInFocus) {
        HelpText(searchText)
    }

    if (findingDishesState.itemCount != 0 || state.value.searchInFocus) {
        VSpacerSmall()
        SearchView(
            lastWords = state.value.lastWords,
            searchText = searchText,
            findingDishesState = findingDishesState,
            listState = listState,
            calculatorFlow = state.value.calculatorFlow,
            lastWordClicked = { word ->
                viewModel sendAction CalculatorAction.LastWordClick(word)
            },
            deleteClick = { dish ->
                viewModel sendAction CalculatorAction.DeleteDishClicked(dish)
            },
            dishesClick = { dish ->
                focusManager.clearFocus()
                viewModel sendAction CalculatorAction.DishClicked(dish)
            }
        )
    } else {
        MainContent(
            dishes = state.value.dishes,
            listState = listState,
            calculatorFlow = state.value.calculatorFlow,
            dishesClick = {
                viewModel sendAction CalculatorAction.DishClicked(it)
            },
            deleteClick = {
                viewModel sendAction CalculatorAction.DeleteDishClicked(it)
            },
            clearListClicked = {
                viewModel sendAction CalculatorAction.ClearList
            }
        )
    }
}

@Composable
private fun CustomProductsBlockInSearch(
    customProductsClicked: () -> Unit,
    createCustomProductClicked: () -> Unit,
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
    listState: LazyListState,
    calculatorFlow: CalculatorFlow,
    dishesClick: (DishUiEntity) -> Unit,
    deleteClick: (DishUiEntity) -> Unit,
    clearListClicked: () -> Unit
) {
    if (dishes.isEmpty()) {
        EmptyContent()
    } else {
        CalculateDishes(
            dishes = dishes,
            listState = listState,
            calculatorFlow = calculatorFlow,
            dishesClick = dishesClick,
            deleteClick = deleteClick,
            clearListClicked = clearListClicked
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

@Composable
private fun CalculateDishes(
    dishes: List<DishUiEntity>,
    listState: LazyListState,
    calculatorFlow: CalculatorFlow,
    dishesClick: (DishUiEntity) -> Unit,
    deleteClick: (DishUiEntity) -> Unit,
    clearListClicked: () -> Unit
) {
    val totalCountBreadUnits =
        dishes.sumOf { it.breadUnits?.toDoubleOrNull() ?: ZERO_COUNT_DOUBLE }.format()

    TotalCountBreadUnits(
        totalCountBreadUnits = totalCountBreadUnits,
        count = dishes.count(),
        calculatorFlow = calculatorFlow,
        clearListClicked = clearListClicked
    )
    VSpacerHalfMedium()
    SelectedDishes(
        dishes = dishes,
        calculatorFlow = calculatorFlow,
        listState = listState,
        dishesClick = dishesClick,
        deleteClick = deleteClick
    )
}

@Composable
private fun TotalCountBreadUnits(
    totalCountBreadUnits: String,
    count: Int,
    calculatorFlow: CalculatorFlow,
    clearListClicked: () -> Unit
) {
    Column {
        TitleSelectedDishes(clearListClicked = clearListClicked)
        CountSelectedDishes(
            count = count,
            calculatorFlow = calculatorFlow,
            totalCountBreadUnits = totalCountBreadUnits
        )
    }
}

@Composable
private fun TitleSelectedDishes(clearListClicked: () -> Unit) {
    GetLocalProperties { dimens, _, colors, _, types ->
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
    }
}

@Composable
private fun CountSelectedDishes(
    count: Int,
    calculatorFlow: CalculatorFlow,
    totalCountBreadUnits: String
) {
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
            Text(
                text = pluralStringResource(
                    id = R.plurals.calculator_total_products, count = count, count
                )
            )
            if (calculatorFlow == CalculatorFlow.BREAD_UNITS) {
                BreadUnitsLabel(totalCountBreadUnits)
            }
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
        MainBlock(viewModel = viewModel())
    }
}
