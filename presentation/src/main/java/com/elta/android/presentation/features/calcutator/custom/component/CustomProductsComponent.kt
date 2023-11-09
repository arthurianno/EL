@file:OptIn(FlowPreview::class)

package com.elta.android.presentation.features.calcutator.custom.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.clickableWithNoRipple
import com.elta.android.presentation.core.compose.common.NetworkState
import com.elta.android.presentation.core.compose.widgets.VSpacer
import com.elta.android.presentation.core.compose.widgets.VSpacerMedium
import com.elta.android.presentation.core.compose.widgets.animation.VerticallyAnimation
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBar
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBarWidgetModel
import com.elta.android.presentation.core.compose.widgets.textfields.SearchField
import com.elta.android.presentation.core.compose.widgets.textfields.SearchFieldWidgetModel
import com.elta.android.presentation.features.calcutator.component.DishesItem
import com.elta.android.presentation.features.calcutator.component.ErrorScreen
import com.elta.android.presentation.features.calcutator.component.LoadingScreen
import com.elta.android.presentation.features.calcutator.component.TrailingIcon
import com.elta.android.presentation.features.calcutator.custom.model.CustomProductAction
import com.elta.android.presentation.features.calcutator.custom.viewmodel.CustomProductsViewModel
import com.elta.android.presentation.features.calcutator.products.component.NotResult
import com.elta.android.presentation.features.calcutator.products.model.DishUiEntity
import com.elta.android.presentation.theme.GetLocalProperties
import com.elta.android.presentation.theme.LocalNetworkState
import kotlinx.coroutines.FlowPreview

@ExperimentalComposeUiApi
@Composable
fun CustomDishes(viewModel: CustomProductsViewModel) {
    val state = viewModel.state.collectAsState().value
    val customProducts = viewModel.customProductsState.collectAsLazyPagingItems()
    val focusManager = LocalFocusManager.current

    if (!state.searchInFocus) {
        focusManager.clearFocus()
    }
    val keyboardController = LocalSoftwareKeyboardController.current
    val searchInFocus = state.searchInFocus
    val networkAvailable = LocalNetworkState.current == NetworkState.Available

    GetLocalProperties { dimens, _, colors, shapes, _ ->
        val systemBarColor = animateColorAsState(
            targetValue = if (searchInFocus) colors.shadeBlack3 else colors.gOrangeA, label = ""
        )
        Box(modifier = Modifier
            .fillMaxSize()
            .background(color = systemBarColor.value)
            .clickableWithNoRipple {
                focusManager.clearFocus()
                keyboardController?.hide()
            }) {
            Scaffold(
                scaffoldState = rememberScaffoldState(),
                topBar = { TopBar(viewModel.appTopBar, searchInFocus) },
                backgroundColor = colors.gOrangeA,
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
                        .padding(
                            start = dimens.contentPadding,
                            top = dimens.contentPadding,
                            end = dimens.contentPadding
                        )
                ) {
                    Content(viewModel, searchInFocus, customProducts, networkAvailable)
                }
            }
        }
    }
}

@Composable
private fun Content(
    viewModel: CustomProductsViewModel,
    searchInFocus: Boolean,
    customProducts: LazyPagingItems<DishUiEntity>,
    networkAvailable: Boolean,
) {
    val searchState = viewModel.searchField.state.collectAsState().value
    val searchText = searchState.textField.text

    GetLocalProperties { dimens, _, _, _, _ ->

        val loadState = customProducts.loadState

        if (loadState.refresh !is LoadState.Error) {
            Header(
                searchFieldWidgetModel = viewModel.searchField,
                searchInFocus = searchInFocus
            ) {
                viewModel.sendAction(CustomProductAction.CreateProduct)
            }

            VSpacerMedium()
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                loadState.refresh is LoadState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize()) { LoadingScreen() }
                }

                loadState.refresh is LoadState.Error -> {
                    viewModel.sendAction(CustomProductAction.ErrorResult)
                    val textId = if (networkAvailable) {
                        R.string.custom_product_list_server_error
                    } else {
                        R.string.custom_product_list_offline_error
                    }
                    ErrorScreen(textId = textId) {
                        customProducts.retry()
                    }
                }

                loadState.refresh is LoadState.NotLoading && customProducts.itemCount == 0 -> {
                    val notResultTextId = if (searchText.isBlank()) {
                        R.string.custom_product_list_is_empty
                    } else {
                        R.string.calculator_search_no_result
                    }
                    NotResult(notResultTextId)
                }

                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(dimens.dishCardVerticalSpace)) {

                        items(customProducts.itemCount) { index ->
                            DishesItem(dish = customProducts[index],
                                trailingIcon = TrailingIcon.CustomDish,
                                dishesClick = { dishClicked ->
                                    viewModel sendAction CustomProductAction.ProductClicked(
                                        dishClicked
                                    )
                                },
                                deleteClick = { deletedDish ->
                                    viewModel sendAction CustomProductAction.DeleteProductClicked(
                                        deletedDish
                                    )
                                })
                        }

                        if (loadState.append is LoadState.Loading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = dimens.contentPadding)
                                        .wrapContentWidth(Alignment.CenterHorizontally)
                                ) {
                                    LoadingScreen()
                                }
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
private fun Header(
    searchFieldWidgetModel: SearchFieldWidgetModel,
    searchInFocus: Boolean,
    createProductCallback: () -> Unit
) {
    GetLocalProperties { dimens, _, _, _, _ ->
        Column {
            SearchField(
                widgetModel = searchFieldWidgetModel,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                searchInFocus = searchInFocus
            )
            VSpacer(dimens.bigDim)
            CreateCustomProductButton {
                createProductCallback()
            }
        }
    }
}

@Composable
private fun CreateCustomProductButton(callback: () -> Unit) {
    GetLocalProperties { dimens, _, colors, shapes, types ->
        Box(modifier = Modifier
            .clip(shapes.textField)
            .background(colors.paleGray)
            .wrapContentWidth(Alignment.CenterHorizontally)
            .clickable { callback() }) {
            Row(
                modifier = Modifier
                    .padding(dimens.contentPadding)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = dimens.smallDim),
                    text = stringResource(R.string.custom_products_create_product),
                    style = types.title3,
                    color = colors.blackBlue,
                )
                Image(
                    modifier = Modifier.size(dimens.dishIconSize),
                    painter = painterResource(id = R.drawable.ic_home_plus),
                    colorFilter = ColorFilter.tint(colors.shadeBlack1),
                    contentDescription = stringResource(R.string.custom_products_create_product)
                )
            }
        }
    }
}

@Composable
private fun TopBar(
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
                startIcon = R.drawable.ic_dialog_close
            )
        }
    }
}

@Composable
fun RowScope.CustomProductButton(textId: Int, iconId: Int, callback: () -> Unit) {
    GetLocalProperties { dimens, _, colors, shapes, types ->
        Box(modifier = Modifier
            .clip(shapes.textField)
            .background(colors.paleGray)
            .weight(1f)
            .clickable { callback() }) {
            Row(
                modifier = Modifier
                    .padding(dimens.halfMediumDim)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier
                        .padding(end = dimens.smallDim)
                        .weight(1f),
                    textAlign = TextAlign.Center,
                    text = stringResource(textId),
                    style = types.caption1,
                    color = colors.blackBlue,
                )

                Image(
                    modifier = Modifier.size(dimens.dishIconSize),
                    painter = painterResource(id = iconId),
                    colorFilter = ColorFilter.tint(colors.shadeBlack1),
                    contentDescription = stringResource(textId)
                )
            }
        }

    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Preview
@Composable
private fun PreviewCustomDishes() {
    CustomDishes(viewModel = viewModel())
}
