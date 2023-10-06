package com.elta.android.presentation.features.calcutator.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.widgets.HSpacerMedium
import com.elta.android.presentation.core.compose.widgets.VSpacer
import com.elta.android.presentation.core.compose.widgets.VSpacerMedium
import com.elta.android.presentation.features.calcutator.model.DishUiEntity
import com.elta.android.presentation.theme.GetLocalProperties

@Composable
//don't delete it.
//stopped by the customer
internal fun FindingDishesWithFatSecret(
    findingDishes: LazyPagingItems<DishUiEntity>,
    verifiedDishes: List<DishUiEntity>,
    dishesClick: (DishUiEntity?) -> Unit,
) {
    GetLocalProperties { dimens, _, _, _, _ ->

        Box(modifier = Modifier.fillMaxSize()) {

            val loadState = findingDishes.loadState

            when {
                loadState.refresh is LoadState.Loading -> CircularProgressIndicator(
                    modifier = Modifier.align(
                        Alignment.Center
                    )
                )

                loadState.refresh is LoadState.Error -> SearchError()
                loadState.refresh is LoadState.NotLoading && findingDishes.itemCount == 0 -> NotResult()
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(dimens.dishCardVerticalSpace)) {

                        items(verifiedDishes) { item ->
                            DishesItem(item, dishesClick)
                        }

                        items(findingDishes.itemCount) { index ->
                            DishesItem(findingDishes[index], dishesClick)
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
internal fun FindingVerifiedDishes(
    verifiedDishes: List<DishUiEntity>,
    isLoading: Boolean,
    isError: Boolean,
    dishesClick: (DishUiEntity?) -> Unit,
) {
    GetLocalProperties { dimens, _, _, _, _ ->

        Box(modifier = Modifier.fillMaxSize()) {

            when {
                isError -> SearchError()
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                verifiedDishes.isEmpty() -> NotResult()
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(dimens.dishCardVerticalSpace)) {
                    items(verifiedDishes) { item ->
                        DishesItem(item, dishesClick)
                    }
                }
            }
            VSpacerMedium()
        }
    }

}

@Composable
private fun DishesItem(
    dish: DishUiEntity?,
    dishesClick: (DishUiEntity?) -> Unit
) {
    GetLocalProperties { dimens, _, colors, shapes, _ ->
        Box(
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
                )
        ) {
            dish?.let { dish ->
                CardBody(dish, trailingIcon = {
                    Image(
                        painter = painterResource(id = R.drawable.btn_plus),
                        contentDescription = null
                    )
                })
            }
        }
    }
}

@Composable
internal fun CardBody(dish: DishUiEntity, trailingIcon: @Composable (Modifier) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Brand(dish.brandName)
            TitleDish(dish)
            Spacer(
                modifier = Modifier
                    .height(16.dp)
                    .weight(1f)
            )
            ServingInfo(dish)
        }
        HSpacerMedium()
        trailingIcon(Modifier.fillMaxHeight())
    }
}

@Composable
private fun TitleDish(dish: DishUiEntity) {
    GetLocalProperties { dimens, _, colors, _, types ->
        val iconId = "iconId"
        val inlineContext = mapOf(
            iconId to InlineTextContent(
                placeholder = Placeholder(
                    width = 20.sp,
                    height = 20.sp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_verify_dish),
                    modifier = Modifier.padding(
                        end = dimens.verySmallDim,
                        top = dimens.smallestDim
                    ),
                    contentDescription = null
                )
            })
        Text(
            text = buildAnnotatedString {
                if (dish.isVerified) {
                    appendInlineContent(id = iconId)
                }
                append(text = dish.name)
            },
            inlineContent = inlineContext,
            style = types.title3,
            color = colors.blackBlue,
            maxLines = THREE_LINES_COUNT,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun Brand(name: String) {
    GetLocalProperties { dimens, _, colors, _, types ->
        if (name.isNotEmpty()) {
            Text(
                text = name,
                style = types.textStyle2,
                color = colors.shadeBlack1,
                maxLines = TWO_LINES_COUNT,
                overflow = TextOverflow.Ellipsis
            )
            VSpacer(height = dimens.smallestDim)
        }
    }
}

@Composable
private fun ServingInfo(dish: DishUiEntity) {
    GetLocalProperties { _, _, colors, _, types ->
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
            if (dish.servingCalories.second.isNotEmpty()) {
                Text(
                    text = stringResource(
                        id = R.string.calculator_dish_calories_in_serving,
                        dish.servingCalories.second
                    ),
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
