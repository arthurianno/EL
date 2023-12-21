package com.elta.android.presentation.features.calcutator.products.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.paging.compose.LazyPagingItems
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.widgets.text.LastWords
import com.elta.android.domain.features.diary.home.model.CalculatorFlow
import com.elta.android.presentation.features.calcutator.component.TrailingIcon
import com.elta.android.presentation.features.calcutator.products.model.DishUiEntity
import com.elta.android.presentation.theme.GetLocalProperties

@Composable
internal fun SearchView(
    lastWords: List<String>,
    searchText: String,
    findingDishesState: LazyPagingItems<DishUiEntity>,
    listState: LazyListState,
    calculatorFlow: CalculatorFlow,
    lastWordClicked: (String) -> Unit,
    dishesClick: (DishUiEntity) -> Unit,
    deleteClick: (DishUiEntity) -> Unit
) {
    if (searchText.isEmpty()) {
        LastWords(
            lastWords = lastWords,
            lastWordClicked = lastWordClicked
        )
    } else {
        FindingDishes(
            dishes = findingDishesState,
            trailingIcon = TrailingIcon.AddDish,
            calculatorFlow = calculatorFlow,
            listState = listState,
            dishesClick = dishesClick,
            deleteClick = deleteClick
        )
    }
}

@Composable
internal fun BoxScope.SearchError() {
    GetLocalProperties { _, _, colors, _, _ ->
        Text(
            text = stringResource(id = R.string.calculator_search_server_unavailable),
            color = colors.shadeBlack2,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
internal fun BoxScope.NotResult(@StringRes textId: Int = R.string.calculator_search_no_result) {
    GetLocalProperties { _, _, colors, _, _ ->
        Text(
            text = stringResource(id = textId),
            color = colors.shadeBlack2,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
