@file:OptIn(ExperimentalComposeUiApi::class)

package com.elta.android.presentation.features.calcutator.custom.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.clickableWithNoRipple
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.NetworkState
import com.elta.android.presentation.core.compose.widgets.HSpacerHalfMedium
import com.elta.android.presentation.core.compose.widgets.VSpacerMedium
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBar
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBarWidgetModel
import com.elta.android.presentation.core.compose.widgets.buttons.DownButton
import com.elta.android.presentation.core.compose.widgets.textfields.InputText
import com.elta.android.presentation.features.calcutator.component.ErrorScreen
import com.elta.android.presentation.features.calcutator.component.LoadingScreen
import com.elta.android.presentation.features.calcutator.component.PortionHelper
import com.elta.android.presentation.features.calcutator.component.PortionProductContent
import com.elta.android.presentation.features.calcutator.component.PortionProductHeader
import com.elta.android.presentation.features.calcutator.custom.model.CreateCustomProductAction
import com.elta.android.presentation.features.calcutator.custom.viewmodel.CreateCustomProductViewModel
import com.elta.android.presentation.theme.GetLocalProperties
import com.elta.android.presentation.theme.LocalNetworkState
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class)
@ExperimentalComposeUiApi
@Composable
fun CreateCustomDishes(
    viewModel: CreateCustomProductViewModel,
) {
    val state = viewModel.state.collectAsState().value
    val networkAvailable = LocalNetworkState.current == NetworkState.Available

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    GetLocalProperties { dimens, _, colors, shapes, _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = colors.gOrangeA)
                .focusRequester(focusRequester)
                .clickableWithNoRipple {
                    keyboardController?.hide()
                    focusRequester.requestFocus()
                    focusManager.clearFocus()
                    viewModel sendAction AppAction.FreeScreenTap
                }
        ) {
            Scaffold(
                scaffoldState = rememberScaffoldState(),
                topBar = { TopBar(viewModel.appTopBar) },
                backgroundColor = colors.gOrangeA,
                modifier = Modifier.statusBarsPadding()
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(
                            color = colors.white, shape = shapes.sheet
                        )
                        .padding(
                            start = dimens.contentPadding,
                            top = dimens.contentPadding,
                            end = dimens.contentPadding
                        )
                ) {

                    when {
                        state.isLoading -> Box(modifier = Modifier.fillMaxSize()) { LoadingScreen() }
                        state.isError -> {
                            val textId = if (networkAvailable)
                                R.string.create_custom_product_server_error
                            else
                                R.string.create_custom_product_offline_error

                            ErrorScreen(textId = textId) {
                                viewModel.sendAction(CreateCustomProductAction.Retry)
                            }
                        }
                        else -> Content(viewModel, focusManager)
                    }

                }

            }
            if (!state.isLoading && !state.isError) {
                DownButton(widgetModel = viewModel.downButton)
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun Content(viewModel: CreateCustomProductViewModel, focusManager: FocusManager) {
    val state = viewModel.state.collectAsState().value
    val numberOfUnitsIsError = viewModel.portionCountTextField.state.collectAsState().value.isError

    InputText(
        widgetModel = viewModel.productNameField,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done
        ),
        focusManager = focusManager,
        isFocusRequested = true
    )

    PortionProductHeader { viewModel sendAction CreateCustomProductAction.PortionHelpClick }
    PortionProductContent(
        portionWidgetModel = viewModel.portionCountTextField,
        portionDescriptionWidgetModel = viewModel.portionDescriptionTextField,
        focusManager = focusManager,
        isError = numberOfUnitsIsError,
        isFocusRequested = false
    )


    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column {
            VSpacerMedium()
            InputText(
                widgetModel = viewModel.breadUnitsField,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done,
                ),
                focusManager = focusManager,
                isFocusRequested = false
            )
        }

        PortionHelper(
            isShowCountHelpSnack = state.isShowCountHelpSnack,
            message = stringResource(id = R.string.calculator_portion_count_and_serving_help_snack)
        )
    }

    VSpacerMedium()
    if (state.dish != null) {
        NonEditable()
    }
}

@Composable
private fun NonEditable() {
    GetLocalProperties { dimens, _, colors, shapes, types ->
        Box(
            modifier = Modifier
                .clip(shapes.textField)
                .background(colors.paleGray)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(dimens.halfMediumDim)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_info_fill),
                    tint = Color.Unspecified,
                    contentDescription = stringResource(id = R.string.custom_products_non_editable)
                )

                HSpacerHalfMedium()

                Text(
                    text = stringResource(id = R.string.custom_products_non_editable),
                    style = types.body1,
                )
            }
        }
    }
}

@ExperimentalComposeUiApi
@Composable

fun Modifier.bottomBorder(strokeWidth: Dp, color: Color) = composed(factory = {
    val density = LocalDensity.current
    val strokeWidthPx = density.run { strokeWidth.toPx() }

    Modifier.drawBehind {
        val width = size.width
        val height = size.height - strokeWidthPx / 2

        drawLine(
            color = color,
            start = Offset(x = 0f, y = height),
            end = Offset(x = width, y = height),
            strokeWidth = strokeWidthPx
        )
    }
})

@Composable
private fun TopBar(
    appTopBarWidgetModel: BaseAppTopBarWidgetModel
) {
    GetLocalProperties { _, _, colors, _, types ->
        BaseAppTopBar(
            widgetModel = appTopBarWidgetModel,
            backgroundColor = colors.gOrangeA,
            textStyle = types.h2,
            textColor = colors.white,
            startIcon = R.drawable.ic_dialog_close
        )
    }
}

@OptIn(FlowPreview::class)
@Preview
@Composable
private fun PreviewCreateCustomDish() {
    CreateCustomDishes(viewModel = viewModel())
}