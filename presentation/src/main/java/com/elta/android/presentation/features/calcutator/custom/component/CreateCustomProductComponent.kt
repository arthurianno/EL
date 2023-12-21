package com.elta.android.presentation.features.calcutator.custom.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.clickableWithNoRipple
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.NetworkState
import com.elta.android.presentation.core.compose.widgets.HSpacerHalfMedium
import com.elta.android.presentation.core.compose.widgets.VSpacer
import com.elta.android.presentation.core.compose.widgets.VSpacerMedium
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBar
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBarWidgetModel
import com.elta.android.presentation.core.compose.widgets.buttons.DownButton
import com.elta.android.presentation.core.compose.widgets.common.ErrorScreen
import com.elta.android.presentation.core.compose.widgets.common.LoadingScreen
import com.elta.android.presentation.core.compose.widgets.textfields.InputText
import com.elta.android.presentation.core.compose.widgets.textfields.InputTextFieldWidgetModel
import com.elta.android.domain.features.diary.home.model.CalculatorFlow
import com.elta.android.presentation.features.calcutator.component.PortionHelper
import com.elta.android.presentation.features.calcutator.component.PortionProductContent
import com.elta.android.presentation.features.calcutator.component.PortionProductHeader
import com.elta.android.presentation.features.calcutator.custom.model.CreateCustomProductAction
import com.elta.android.presentation.features.calcutator.custom.model.CreateCustomProductFlow
import com.elta.android.presentation.features.calcutator.custom.model.CreateCustomProductFlow.Companion.isCreating
import com.elta.android.presentation.features.calcutator.custom.viewmodel.CreateCustomProductViewModel
import com.elta.android.presentation.features.calcutator.products.component.DishChars
import com.elta.android.presentation.theme.GetLocalProperties
import com.elta.android.presentation.theme.LocalNetworkState

@ExperimentalComposeUiApi
@Composable
fun CreateCustomDishes(viewModel: CreateCustomProductViewModel) {
    val state = viewModel.state.collectAsState().value
    val networkAvailable = LocalNetworkState.current == NetworkState.Available

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    GetLocalProperties { dimens, _, colors, shapes, _ ->
        Scaffold(
            scaffoldState = rememberScaffoldState(),
            topBar = { TopBar(viewModel.appTopBar) },
            bottomBar = { DownButton(widgetModel = viewModel.downButton) },
            backgroundColor = colors.gOrangeA,
            modifier = Modifier
                .focusRequester(focusRequester)
                .clickableWithNoRipple {
                    keyboardController?.hide()
                    focusRequester.requestFocus()
                    focusManager.clearFocus()
                    viewModel sendAction AppAction.FreeScreenTap
                }
                .background(color = colors.gOrangeA)
                .statusBarsPadding()
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(color = colors.white, shape = shapes.sheet)
                    .padding(dimens.contentListPadding)
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                verticalArrangement = if (state.isLoading || state.isError) Arrangement.Center else Arrangement.Top
            ) {

                when {
                    state.isLoading -> LoadingScreen(color = colors.shadeBlack1)
                    state.isError -> {
                        val textId =
                            if (networkAvailable) R.string.create_custom_product_server_error
                            else R.string.create_custom_product_offline_error

                        ErrorScreen(titleTextId = textId) {
                            viewModel.sendAction(CreateCustomProductAction.Retry)
                        }
                    }

                    else -> Content(viewModel, focusManager)
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun Content(viewModel: CreateCustomProductViewModel, focusManager: FocusManager) {
    val state = viewModel.state.collectAsState().value
    val numberOfUnitsIsError = viewModel.portionCountTextField.state.collectAsState().value.isError

    GetLocalProperties { dimens, _, _, _, _ ->
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
        VSpacer(height = dimens.halfBigDim)
        PortionProductHeader(isQuestionButtonVisible = state.createCustomProductFlow.isCreating()) {
            viewModel sendAction CreateCustomProductAction.PortionHelpClick
        }
        if (!state.createCustomProductFlow.isCreating()) VSpacerMedium()
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
                if (state.calculatorFlow == CalculatorFlow.BREAD_UNITS) {
                    InputText(
                        widgetModel = viewModel.specialCarbohydrateField,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done,
                        ),
                        focusManager = focusManager,
                        isFocusRequested = false
                    )
                }
                if (state.createCustomProductFlow.isCreating()) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        ExtraInfo(
                            carbohydrateField = viewModel.carbohydrateField,
                            caloriesField = viewModel.caloriesField,
                            fatField = viewModel.fatField,
                            proteinField = viewModel.proteinField,
                            calculatorFlow = state.calculatorFlow
                        )
                        PortionHelper(
                            isShowCountHelpSnack = state.isShowCarbohydrateCountHelpSnack,
                            message = stringResource(id = R.string.custom_product_input_units_must_be_add)
                        )
                    }
                } else {
                    Column {
                        VSpacerMedium()
                        state.dish?.servingSelect?.let { DishChars(it) }
                    }
                }
            }

            PortionHelper(
                isShowCountHelpSnack = state.isShowServingCountHelpSnack,
                message = stringResource(id = R.string.calculator_portion_count_and_serving_help_snack)
            )
        }

        VSpacerMedium()
        if (!state.createCustomProductFlow.isCreating()) {
            NonEditable()
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ExtraInfo(
    carbohydrateField: InputTextFieldWidgetModel,
    caloriesField: InputTextFieldWidgetModel,
    proteinField: InputTextFieldWidgetModel,
    fatField: InputTextFieldWidgetModel,
    calculatorFlow: CalculatorFlow
) {
    GetLocalProperties { dimens, _, colors, _, types ->
        Column {
            VSpacer(dimens.halfBigDim)
            Text(
                text = stringResource(id = R.string.custom_product_extra_info),
                style = types.body2,
                color = colors.black
            )
            if (calculatorFlow == CalculatorFlow.PRODUCT_ONLY) {
                InputText(
                    widgetModel = carbohydrateField,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    leadingIcon = {
                        LetterIcon(stringResource(id = R.string.custom_product_extra_letter_for_carbohydrate))
                    }
                )
            }
            InputText(
                widgetModel = caloriesField,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                leadingIcon = {
                    LetterIcon(stringResource(id = R.string.custom_product_extra_letter_for_calories))
                }
            )
            InputText(
                widgetModel = proteinField,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                leadingIcon = {
                    LetterIcon(stringResource(id = R.string.custom_product_extra_letter_for_protein))
                }
            )
            InputText(
                widgetModel = fatField,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                leadingIcon = {
                    LetterIcon(stringResource(id = R.string.custom_product_extra_letter_for_fat))
                }
            )
        }
    }
}

@ExperimentalComposeUiApi
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

@ExperimentalComposeUiApi
@Composable
private fun TopBar(appTopBarWidgetModel: BaseAppTopBarWidgetModel) {
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

@ExperimentalComposeUiApi
@Composable
private fun LetterIcon(letter: String) {
    GetLocalProperties { dimens, _, colors, _, types ->
        Text(
            modifier = Modifier
                .padding(dimens.letterIcon)
                .drawBehind {
                    drawCircle(
                        color = colors.gGreenB,
                        radius = this.size.maxDimension * 0.75f
                    )
                },
            text = letter,
            color = colors.white,
            style = types.title3,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Preview
@Composable
private fun PreviewCreateCustomDish() {
    CreateCustomDishes(viewModel = viewModel())
}
