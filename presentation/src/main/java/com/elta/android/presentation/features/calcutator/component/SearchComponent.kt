package com.elta.android.presentation.features.calcutator.component

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.widgets.VSpacer
import com.elta.android.presentation.core.compose.widgets.VSpacerMedium
import com.elta.android.presentation.features.calcutator.model.DishUiEntity
import com.elta.android.presentation.theme.GetLocalProperties

@Composable
internal fun FindingDishes(
    findingDishes: LazyPagingItems<DishUiEntity>,
    dishesClick: (DishUiEntity?) -> Unit,
) {
    GetLocalProperties { dimens, _, colors, shapes, _ ->

        Box(modifier = Modifier.fillMaxSize()) {

            val loadState = findingDishes.loadState

            when {
                loadState.refresh is LoadState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                loadState.refresh is LoadState.Error -> SearchError()
                loadState.refresh is LoadState.NotLoading && findingDishes.itemCount == 0 -> NotResult()
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(dimens.dishCardVerticalSpace)) {

                        items(findingDishes.itemCount) { index ->
                            DishesItem(findingDishes, index, dishesClick)
                        }

                        if (loadState.append is LoadState.Loading) {
                            item {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = dimens.contentPadding)
                                        .wrapContentWidth(Alignment.CenterHorizontally)
                                )
                            }
                        }

                    }
                }
            }
            VSpacerMedium()
        }
    }

}

@Composable
private fun DishesItem(
    findingDishes: LazyPagingItems<DishUiEntity>,
    index: Int,
    dishesClick: (DishUiEntity?) -> Unit
) {
    val dish = findingDishes[index]
    GetLocalProperties { dimens, _, colors, shapes, _ ->

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shapes.dishCard)
                .clickable {
                    dishesClick(dish)
                }
                .border(
                    dimens.borderWidth,
                    colors.shadeBlack3,
                    shapes.dishCard
                )
                .padding(
                    horizontal = dimens.contentPadding,
                    vertical = dimens.halfMediumDim
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            dish?.let { dish -> CardBody(dish) }
            Image(
                painter = painterResource(id = R.drawable.btn_plus),
                contentDescription = null
            )
        }
    }
}

@Composable
internal fun RowScope.CardBody(dish: DishUiEntity) {
    GetLocalProperties { dimens, _, colors, _, types ->
        Column(
            modifier = Modifier
                .padding(end = dimens.contentPadding)
                .weight(1f)
        ) {
            val brandNameExist = dish.brandName.isNotEmpty()
            if (brandNameExist) {
                Text(
                    text = dish.brandName,
                    style = types.textStyle2,
                    color = colors.shadeBlack1,
                    maxLines = TWO_LINES_COUNT,
                    overflow = TextOverflow.Ellipsis
                )
                VSpacer(height = dimens.smallestDim)
            }
            Text(
                text = dish.name,
                style = types.title3,
                color = colors.blackBlue,
                maxLines = THREE_LINES_COUNT,
                overflow = TextOverflow.Ellipsis
            )
            VSpacer(height = dimens.halfMediumDim)
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    modifier = Modifier.weight(weight = 1F, fill = false),
                    text = dish.servingCalories.first,
                    style = types.textStyle2,
                    color = colors.shadeBlack1,
                    maxLines = SINGLE_LINE_COUNT,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(id = R.string.calculator_dish_calories_in_serving, dish.servingCalories.second),
                    style = types.textStyle2,
                    color = colors.shadeBlack1,
                    maxLines = SINGLE_LINE_COUNT
                )
            }
        }
    }
}
@Composable
private fun BoxScope.NotResult() {
    GetLocalProperties { _, _, colors, _, _ ->
        Text(
            text = stringResource(id = R.string.calculator_search_no_result),
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

private const val SINGLE_LINE_COUNT = 1
private const val TWO_LINES_COUNT = 2
private const val THREE_LINES_COUNT = 3
