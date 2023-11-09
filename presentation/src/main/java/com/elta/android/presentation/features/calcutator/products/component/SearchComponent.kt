package com.elta.android.presentation.features.calcutator.products.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.widgets.VSpacerMedium
import com.elta.android.presentation.core.compose.widgets.paging.ErrorNextPage
import com.elta.android.presentation.core.compose.widgets.paging.LoadingNextPage
import com.elta.android.presentation.features.calcutator.component.DishesItem
import com.elta.android.presentation.features.calcutator.component.LoadingScreen
import com.elta.android.presentation.features.calcutator.component.TrailingIcon
import com.elta.android.presentation.features.calcutator.products.model.DishUiEntity
import com.elta.android.presentation.theme.GetLocalProperties

@Composable
internal fun FindingDishes(
    dishes: LazyPagingItems<DishUiEntity>,
    trailingIcon: TrailingIcon,
    dishesClick: (DishUiEntity) -> Unit,
    deleteClick: (DishUiEntity) -> Unit,
    listState: LazyListState,
) {
    GetLocalProperties { dimens, _, colors, _, _ ->

        Box(modifier = Modifier.fillMaxSize()) {

            val loadState = dishes.loadState

            when {
                loadState.refresh is LoadState.Loading -> LoadingScreen()

                loadState.refresh is LoadState.Error -> SearchError()
                loadState.refresh is LoadState.NotLoading && dishes.itemCount == 0 -> NotResult()
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(dimens.dishCardVerticalSpace),
                        state = listState
                    ) {

                        items(dishes.itemCount) { index ->
                            DishesItem(dishes[index], trailingIcon, dishesClick, deleteClick)
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
                        DishesItem(item, TrailingIcon.CustomDish, dishClicked, deleteDish)
                    }
                }
                VSpacerMedium()

            }

            else -> {
                Text(stringResource(id = R.string.custom_product_empty_list))
            }

        }

    }
}

@Composable
fun BoxScope.NotResult(
    @StringRes textId: Int = R.string.calculator_search_no_result
) {
    GetLocalProperties { _, _, colors, _, _ ->
        Text(
            text = stringResource(id = textId),
            color = colors.shadeBlack2,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun BoxScope.SearchError() {
    GetLocalProperties { _, _, colors, _, _ ->
        Text(
            text = stringResource(id = R.string.calculator_search_server_unavailable),
            color = colors.shadeBlack2,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}