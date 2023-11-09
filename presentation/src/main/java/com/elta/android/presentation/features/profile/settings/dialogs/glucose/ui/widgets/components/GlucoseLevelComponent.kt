package com.elta.android.presentation.features.profile.settings.dialogs.glucose.ui.widgets.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.widgets.HSpacerSmall
import com.elta.android.presentation.core.compose.widgets.VSpacer
import com.elta.android.presentation.core.compose.widgets.VSpacerSmall
import com.elta.android.presentation.core.compose.widgets.animation.VerticallyAnimation
import com.elta.android.presentation.core.compose.widgets.textfields.IconOutlinedTextField
import com.elta.android.presentation.core.compose.widgets.textfields.IconOutlinedTextFieldWidgetModel
import com.elta.android.presentation.features.profile.settings.dialogs.glucose.model.GlucoseRangeError
import com.elta.android.presentation.features.profile.settings.dialogs.glucose.model.getMessageByError
import com.elta.android.presentation.features.profile.settings.dialogs.glucose.model.isError
import com.elta.android.presentation.features.profile.settings.dialogs.glucose.model.isErrorWithoutOutOfRange
import com.elta.android.presentation.theme.GetLocalProperties

@Composable
fun GlucoseLevelCard(
    title: String,
    errorType: GlucoseRangeError,
    minWidgetModel: IconOutlinedTextFieldWidgetModel,
    maxWidgetModel: IconOutlinedTextFieldWidgetModel,
    imeAction: ImeAction = ImeAction.Next
) {
    GetLocalProperties { dimens, _, colors, shapes, types ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shapes.textField)
                .border(
                    width = dimens.borderWidth,
                    color = colors.shadeBlack3,
                    shape = shapes.textField
                )
                .padding(dimens.glucoseRangeCardPadding)
                .background(colors.white)
        ) {
            Text(
                text = title,
                style = types.h3
            )
            VSpacer(height = dimens.glucoseRangeTextPadding)
            RangeRow(
                minWidget = minWidgetModel,
                maxWidget = maxWidgetModel,
                errorType = errorType,
                imeAction = imeAction
            )
            VSpacerSmall()
            VerticallyAnimation(visualState = errorType.isError()) {
                Text(
                    text = errorType.getMessageByError(),
                    style = types.caption1,
                    color = colors.red
                )
            }
        }
    }
}

@Composable
fun RangeRow(
    minWidget: IconOutlinedTextFieldWidgetModel,
    maxWidget: IconOutlinedTextFieldWidgetModel,
    errorType: GlucoseRangeError,
    imeAction: ImeAction = ImeAction.Next
) {
    GetLocalProperties { dimens, _, colors, _, types ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ValueField(
                widgetModel = minWidget,
                errorType = errorType
            )
            Text(
                text = stringResource(id = R.string.profile_settings_glucose_range_dash),
                style = types.infoDialog,
                color = colors.black,
                modifier = Modifier.padding(horizontal = dimens.smallDim)
            )
            ValueField(
                widgetModel = maxWidget,
                errorType = errorType,
                imeAction = imeAction
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RowScope.ValueField(
    widgetModel: IconOutlinedTextFieldWidgetModel,
    errorType: GlucoseRangeError,
    imeAction: ImeAction = ImeAction.Next
) {
    GetLocalProperties { _, _, colors, _, types ->
        val state = widgetModel.state.collectAsState()
        widgetModel.setError(errorType.isErrorWithoutOutOfRange(state.value.textField.text))

        IconOutlinedTextField(
            widgetModel = widgetModel,
            textStyle = types.h2.copy(textAlign = TextAlign.Center),
            imeAction = imeAction,
            fieldColors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = colors.shadeGGreen2A,
                placeholderColor = colors.shadeBlack2,
                disabledTextColor = colors.blackBlue,

                backgroundColor = colors.ghostWhite,

                focusedBorderColor = colors.shadeBlack2,
                unfocusedBorderColor = colors.shadeBlack3,

                cursorColor = colors.shadeGGreen2A,
                errorCursorColor = colors.red,

                errorBorderColor = colors.red,
                disabledBorderColor = colors.shadeBlack3,
            )
        )
    }
}

@Composable
fun RemarkLabel() {
    GetLocalProperties { _, _, colors, _, types ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_info),
                contentDescription = null
            )
            HSpacerSmall()
            Text(
                text = stringResource(id = R.string.profile_settings_glucose_remark),
                style = types.caption1,
                color = colors.shadeBlack2
            )
        }
    }
}

@Preview
@Composable
private fun PreviewGlucoseLevelCard() {
    Box(
        modifier = Modifier
            .background(color = Color.White)
            .padding(8.dp)
    ) {
        val stubViewModel = IconOutlinedTextFieldWidgetModel()
        stubViewModel.setText("1.0")
        GlucoseLevelCard(
            title = "До еды",
            errorType = GlucoseRangeError.NONE,
            minWidgetModel = stubViewModel,
            maxWidgetModel = stubViewModel
        )
    }
}

@Preview
@Composable
private fun PreviewRemarkLabel() {
    Box(
        modifier = Modifier
            .background(color = Color.White)
            .padding(8.dp)
    ) {
        RemarkLabel()
    }
}

@Preview
@Composable
private fun PreviewRangeRow() {
    Box(
        modifier = Modifier
            .background(color = Color.White)
            .padding(8.dp)
    ) {
        val stub = IconOutlinedTextFieldWidgetModel()
        stub.setText("10.5")
        RangeRow(
            minWidget = stub,
            maxWidget = stub,
            errorType = GlucoseRangeError.NONE
        )
    }
}

@Preview
@Composable
private fun PreviewValueField() {
    Row(
        modifier = Modifier
            .background(color = Color.White)
            .padding(8.dp)
    ) {
        val stub = IconOutlinedTextFieldWidgetModel()
        stub.setText("19.9")
        ValueField(stub, GlucoseRangeError.NONE)
    }
}
