package com.elta.android.presentation.features.calcutator.products.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.widgets.VSpacerMedium
import com.elta.android.presentation.core.compose.widgets.VSpacerVerySmall
import com.elta.android.presentation.core.compose.widgets.common.LoadingScreen
import com.elta.android.presentation.core.compose.widgets.paging.ErrorNextPage
import com.elta.android.presentation.core.compose.widgets.paging.LoadingNextPage
import com.elta.android.domain.features.diary.home.model.CalculatorFlow
import com.elta.android.presentation.features.calcutator.component.DishesItem
import com.elta.android.presentation.features.calcutator.component.TrailingIcon
import com.elta.android.presentation.features.calcutator.products.model.DishUiEntity
import com.elta.android.presentation.theme.GetLocalProperties

@Composable
internal fun FindingDishes(
    dishes: LazyPagingItems<DishUiEntity>,
    trailingIcon: TrailingIcon,
    calculatorFlow: CalculatorFlow,
    dishesClick: (DishUiEntity) -> Unit,
    deleteClick: (DishUiEntity) -> Unit,
    listState: LazyListState,
) {
    GetLocalProperties { dimens, _, colors, _, _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            val loadState = dishes.loadState
            when {
                loadState.refresh is LoadState.Loading -> LoadingScreen(color = colors.shadeBlack1)

                loadState.refresh is LoadState.Error -> SearchError()
                loadState.refresh is LoadState.NotLoading && dishes.itemCount == 0 -> NotResult()
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(dimens.dishCardVerticalSpace),
                        state = listState
                    ) {

                        items(dishes.itemCount) { index ->
                            DishesItem(
                                dish = dishes[index],
                                isSelectedDish = false,
                                calculatorFlow = calculatorFlow,
                                trailingIcon = trailingIcon,
                                dishesClick = dishesClick,
                                deleteClick = deleteClick
                            )
                        }

                        when(loadState.append) {
                            is LoadState.Error -> item { ErrorNextPage { dishes.retry() } }
                            LoadState.Loading -> item { LoadingNextPage() }
                            is LoadState.NotLoading -> {}
                        }
                    }
                }
            }
            VSpacerMedium()
        }
    }
}

@Composable
internal fun SelectedDishes(
    dishes: List<DishUiEntity>,
    calculatorFlow: CalculatorFlow,
    listState: LazyListState,
    dishesClick: (DishUiEntity) -> Unit,
    deleteClick: (DishUiEntity) -> Unit
) {
    GetLocalProperties { dimens, _, _, _, _ ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(dimens.smallDim),
            state = listState
        ) {
            items(items = dishes) { dish ->
                DishesItem(
                    dish = dish,
                    isSelectedDish = true,
                    trailingIcon = TrailingIcon.BreadUnit,
                    calculatorFlow = calculatorFlow,
                    dishesClick = dishesClick,
                    deleteClick = deleteClick
                )
            }
            item { VSpacerVerySmall() }
        }
    }
}

@Composable
private fun Dishes(
    dishes: List<DishUiEntity>?,
    dishClicked: (DishUiEntity) -> Unit,
    deleteDish: (DishUiEntity) -> Unit
) {
    GetLocalProperties { dimens, _, _, _, _ ->
        when {
            dishes != null && dishes.isEmpty() -> {
                Text(stringResource(id = R.string.custom_product_empty_list))
            }
            dishes != null -> {

                LazyColumn(verticalArrangement = Arrangement.spacedBy(dimens.dishCardVerticalSpace)) {
                    items(dishes) { item ->
                        DishesItem(item, CalculatorFlow.BREAD_UNITS, true, TrailingIcon.CustomDish, dishClicked, deleteDish)
                    }
                }
                VSpacerMedium()
            }
            else -> Text(stringResource(id = R.string.custom_product_empty_list))
        }
    }
}
