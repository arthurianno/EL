package com.elta.android.presentation.features.main.events.selector.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.widgets.HSpacerSmall
import com.elta.android.presentation.core.compose.widgets.VSpacerMedium
import com.elta.android.presentation.core.compose.widgets.VSpacerSmall
import com.elta.android.presentation.features.main.events.selector.model.EventSelectorUi
import com.elta.android.presentation.features.main.events.selector.model.EventSelectorViewState
import com.elta.android.presentation.theme.GetLocalProperties

@Composable
fun EventBlock(
    value: EventSelectorViewState,
    listState: LazyListState,
    isSearchTextEmpty: Boolean,
    onSelectionClick: (Long) -> Unit = {},
    onRecentQueryClick: (Pair<Long, String>) -> Unit = {}
) {
    val textId = if (!isSearchTextEmpty) R.string.calculator_search_result
    else R.string.event_selector_medicine_subtitle

    GetLocalProperties { dimens, _, _, _, _ ->
        LazyColumn(
            state = listState
        ) {
            item {
                if (value.recentlySelection.any { it.isVisible }) {
                    RecentQueriesRow(value, onRecentQueryClick)
                    VSpacerMedium()
                }
                Subtitle(
                    text = stringResource(id = textId),
                    paddingValues = PaddingValues(top = dimens.smallDim)
                )
            }
            items(value.selection, key = { it.id }) { item ->
                if (item.isVisible)
                    SelectionValue(item, onSelectionClick)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LazyItemScope.RecentQueriesRow(items: EventSelectorViewState, onRecentQueryClick:  (Pair<Long, String>) -> Unit) {
    GetLocalProperties { dimens, _, _, _, _ ->
        Column(modifier = Modifier.animateItem()) {
            Subtitle(
                text = stringResource(id = R.string.event_selector_recent_search_subtitle),
                paddingValues = PaddingValues(top = dimens.smallDim, bottom = dimens.verySmallDim)
            )
            VSpacerSmall()
            LazyRow(horizontalArrangement = Arrangement.spacedBy(dimens.smallDim)) {
                item { HSpacerSmall() }
                items(items = items.recentlySelection) { item ->
                    RecentQuery(item, onRecentQueryClick)
                }
                item { HSpacerSmall() }
            }
        }
    }
}

@Composable
private fun Subtitle(text: String, paddingValues: PaddingValues = PaddingValues()) {
    GetLocalProperties { dimens, _, colors, _, types ->
        Text(
            text = text,
            style = types.subtitle1,
            color = colors.shadeBlack2,
            modifier = Modifier
                .padding(horizontal = dimens.contentPadding)
                .padding(paddingValues)
        )
    }
}

@Composable
private fun RecentQuery(item: EventSelectorUi, onRecentQueryClick:  (Pair<Long, String>) -> Unit) {
    GetLocalProperties { dimens, _, colors, shapes, types ->
        Text(
            text = item.name,
            color = colors.shadeBlue2,
            style = types.title2,
            modifier = Modifier
                .clip(shapes.recentQuery)
                .background(colors.lightBlue)
                .clickable { onRecentQueryClick(item.id to item.name) }
                .padding(dimens.recentlySelection)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LazyItemScope.SelectionValue(item: EventSelectorUi, onSelectionClick: (Long) -> Unit) {
    GetLocalProperties { dimens, _, colors, _, types ->
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectionClick(item.id) }
                    .padding(dimens.selectionValue)
                    .animateItem(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.name,
                    color = colors.blackBlue,
                    style = types.subtitle1,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                AnimatedVisibility(
                    visible = item.isSelected,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_select),
                        contentDescription = null
                    )
                }
            }
            Divider(
                color = colors.shadeBlack3,
                thickness = dimens.oneDim,
                modifier = Modifier.padding(horizontal = dimens.contentPadding)
            )
            if (item.hasHint) {
                VSpacerSmall()
                Text(
                    text = stringResource(id = R.string.event_selector_medicine_select_other),
                    color = colors.shadeBlack2,
                    style = types.caption1,
                    modifier = Modifier.padding(horizontal = dimens.contentPadding)
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewEventBlock() {
    EventBlock(
        EventSelectorViewState(
            isLoading = false,
            isError = false,
            searchInFocus = false,
            lastSearchers = emptyList(),
            recentlySelection = emptyList(),
            selection = emptyList(),
            previousSelection = null
        ),
        listState = rememberLazyListState(),
        true
    )
}
