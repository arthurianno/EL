package com.elta.android.presentation.features.calcutator.component

import androidx.annotation.StringRes
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.NetworkState
import com.elta.android.presentation.core.compose.widgets.HSpacerMedium
import com.elta.android.presentation.core.compose.widgets.HSpacerSmall
import com.elta.android.presentation.core.compose.widgets.VSpacer
import com.elta.android.presentation.core.compose.widgets.VSpacerHalfLarge
import com.elta.android.presentation.core.compose.widgets.VSpacerMedium
import com.elta.android.presentation.core.compose.widgets.animation.VerticallyAnimation
import com.elta.android.presentation.core.compose.widgets.buttons.ButtonCircle
import com.elta.android.presentation.core.compose.widgets.common.RefreshButton
import com.elta.android.presentation.core.compose.widgets.text.BreadUnitsLabel
import com.elta.android.presentation.core.compose.widgets.textfields.DropdownField
import com.elta.android.presentation.core.compose.widgets.textfields.DropdownFieldWidgetModel
import com.elta.android.presentation.core.compose.widgets.textfields.IconOutlinedTextField
import com.elta.android.presentation.core.compose.widgets.textfields.IconOutlinedTextFieldWidgetModel
import com.elta.android.presentation.features.calcutator.products.component.DishChars
import com.elta.android.presentation.features.calcutator.products.model.DishUiEntity
import com.elta.android.presentation.theme.GetLocalProperties
import com.elta.android.presentation.theme.LocalNetworkState

sealed class TrailingIcon {
    object BreadUnit : TrailingIcon()
    object AddDish : TrailingIcon()
    object CustomDish : TrailingIcon()
}

@Composable
fun BoxScope.LoadingScreen() {
    GetLocalProperties { _, _, colors, _, _ ->
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center),
            color = colors.shadeBlack1
        )
    }
}

@Composable
fun DishesItem(
    dish: DishUiEntity?,
    trailingIcon: TrailingIcon,
    dishesClick: (DishUiEntity) -> Unit,
    deleteClick: (DishUiEntity) -> Unit
) {
    GetLocalProperties { dimens, _, colors, shapes, _ ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shapes.dishCard)
                .clickable { dish?.let { dishesClick(it) } }
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
                CardBody(dish, trailingIcon, deleteClick)
            }
        }
    }
}

@Composable
internal fun CardBody(
    dish: DishUiEntity,
    trailingIcon: TrailingIcon,
    deleteClick: (DishUiEntity) -> Unit
) {
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
        SetIcon(dish = dish, trailingIcon = trailingIcon, deleteClick = deleteClick)
    }
}

@Composable
fun SetIcon(
    dish: DishUiEntity,
    trailingIcon: TrailingIcon,
    deleteClick: (DishUiEntity) -> Unit
) {
    val networkAvailable = LocalNetworkState.current == NetworkState.Available

    when (trailingIcon) {

        TrailingIcon.AddDish -> {
            Image(
                painter = painterResource(id = R.drawable.btn_plus),
                contentDescription = null
            )
        }

        TrailingIcon.BreadUnit -> {
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                if (networkAvailable) {
                    ButtonCircle(
                        icon = R.drawable.btn_close,
                        onClick = {
                            deleteClick.invoke(dish)
                        },
                        contentDescriptionId = R.string.content_description_close_button
                    )
                }
                BreadUnitsLabel(breadUnitsCount = dish.breadUnits)
            }
        }

        TrailingIcon.CustomDish -> {
            ButtonCircle(
                icon = R.drawable.btn_garbage,
                onClick = {
                    deleteClick.invoke(dish)
                },
                contentDescriptionId = R.string.content_description_action_button
            )
        }
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
                        id = R.string.calculator_dish_bread_units_in_serving,
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
fun PortionProductHeader(callback: () -> Unit) {
    GetLocalProperties { _, _, colors, _, types ->
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.calculator_product_portion),
                style = types.subtitle1,
                color = colors.shadeBlack2
            )
            ButtonCircle(
                icon = R.drawable.btn_query_in_circle,
                onClick = { callback() },
                contentDescriptionId = R.string.content_description_question_button
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PortionProductContent(
    portionWidgetModel: IconOutlinedTextFieldWidgetModel,
    portionDescriptionWidgetModel: DropdownFieldWidgetModel,
    focusManager: FocusManager,
    focusRequester: FocusRequester = FocusRequester(),
    isError: Boolean,
    isFocusRequested: Boolean
) {
    GetLocalProperties { dimens, _, colors, _, types ->

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        ) {
            IconOutlinedTextField(
                widgetModel = portionWidgetModel,
                focusRequester = focusRequester,
                focusManager = focusManager,
                imeAction = ImeAction.Done,
                fieldColors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = colors.blackBlue,
                    placeholderColor = colors.shadeBlack2,
                    disabledTextColor = colors.blackBlue,

                    backgroundColor = colors.ghostWhite,

                    focusedBorderColor = colors.shadeBlack2,
                    unfocusedBorderColor = colors.shadeBlack3,
                    errorBorderColor = colors.gOrangeB,
                    disabledBorderColor = colors.shadeBlack3,

                    cursorColor = colors.blackBlue,
                    errorCursorColor = colors.gOrangeB,
                ),
                hint = stringResource(R.string.calculator_serving_count_hint),
                isFocusRequested = isFocusRequested
            )

            HSpacerSmall()

            DropdownField(
                widgetModel = portionDescriptionWidgetModel,
                focusManager = focusManager,
                focusRequester = focusRequester
            )
        }

        if (isError) {
            Text(
                modifier = Modifier.padding(vertical = dimens.smallDim),
                text = stringResource(id = R.string.custom_product_input_units_more_zero),
                style = types.descriptionError
            )

        }
    }

}

@Preview
@Composable
private fun PreviewPortionProductContent() {
    Column {
        PortionProductContent(
            IconOutlinedTextFieldWidgetModel(),
            DropdownFieldWidgetModel(),
            LocalFocusManager.current,
            FocusRequester(),
            isError = false,
            isFocusRequested = false
        )
    }
}

@Composable
fun PortionProductFooter(dish: DishUiEntity, isShowCountHelpSnack: Boolean) {
    val isSingleServing = dish.servings.size == 1

    val textHelpId = if (isSingleServing) R.string.calculator_portion_count_help_snack
    else R.string.calculator_portion_count_and_serving_help_snack

    val snackAlignment = if (isSingleServing) Alignment.TopStart else Alignment.TopCenter

    GetLocalProperties { _, _, colors, _, _ ->
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = snackAlignment
        ) {
            Column {
                VSpacerHalfLarge()
                Text(
                    text = stringResource(id = R.string.calculator_additional_information),
                    color = colors.shadeBlack2,
                )
                DishChars(dish.servingSelect)
                if (dish.isVerified) {
                    VSpacerMedium()
                    VerifyProduct()
                }
            }
            PortionHelper(
                isShowCountHelpSnack = isShowCountHelpSnack,
                message = stringResource(id = textHelpId)
            )
        }
    }
}

@Composable
fun PortionHelper(
    isShowCountHelpSnack: Boolean,
    message: String
) {
    GetLocalProperties { _, _, colors, _, types ->
        VerticallyAnimation(visualState = isShowCountHelpSnack) {
            Box(
                modifier = Modifier
                    .paint(painterResource(id = R.drawable.bg_portion_count_help_snack)),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = message,
                    color = colors.white,
                    style = types.caption1,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(
                        start = 12.dp,
                        bottom = 8.dp,
                        end = 12.dp,
                        top = 14.dp
                    )
                )
            }
        }
    }
}

@Composable
private fun VerifyProduct() {
    GetLocalProperties { dimens, _, colors, _, _ ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.ic_verify_dish),
                contentDescription = null,
                modifier = Modifier.padding(end = dimens.smallDim),
            )
            Text(
                text = stringResource(id = R.string.calculator_verify_product),
                color = colors.blackBlue,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2
            )
        }
    }
}

@Composable
internal fun ErrorScreen(@StringRes textId: Int, retryCallback: () -> Unit) {
    GetLocalProperties { dimens, _, colors, _, types ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
        ) {
            Text(
                text = stringResource(id = textId),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(dimens.bigDim),
                style = types.body1,
                color = colors.shadeBlack1,
                textAlign = TextAlign.Center
            )

            RefreshButton(
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                retryCallback()
            }
        }
    }
}


@Preview
@Composable
private fun PreviewErrorScreen() {
    ErrorScreen(textId = R.string.custom_product_list_server_error) {}
}

private const val SINGLE_LINE_COUNT = 1
private const val TWO_LINES_COUNT = 2
private const val THREE_LINES_COUNT = 3
