package com.elta.android.presentation.features.profile.settings.emias.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.MaskVisualTransformation
import com.elta.android.presentation.core.compose.widgets.HSpacer
import com.elta.android.presentation.core.compose.widgets.VSpacer
import com.elta.android.presentation.core.compose.widgets.textfields.InputText
import com.elta.android.presentation.core.compose.widgets.textfields.InputTextFieldWidgetModel
import com.elta.android.presentation.features.profile.settings.emias.model.EmiasProfileViewState
import com.elta.android.presentation.features.profile.settings.emias.model.NotificationLabelOption
import com.elta.android.presentation.features.profile.settings.emias.model.OMS_RANGE
import com.elta.android.presentation.theme.GetLocalProperties

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EmiasProfileContent(
    modifier: Modifier = Modifier,
    omsInput: InputTextFieldWidgetModel,
    dateInput: InputTextFieldWidgetModel,
    state: EmiasProfileViewState
) {
    val omsMask =
        if (state.oms.length == OMS_RANGE.first) OMS_OLD_MASK
        else OMS_MASK

    GetLocalProperties { dimens, _, colors, _, styles ->

        Column(
            modifier = modifier
                .padding(horizontal = dimens.contentPadding),
        ) {
            Text(
                text = stringResource(id = R.string.profile_emais_title),
                style = styles.h1,
                color = colors.blackBlue
            )
            VSpacer(dimens.smallDim)
            Text(
                text = stringResource(id = R.string.on_boarding_emias_text),
                style = styles.body2,
                color = colors.shadeBlack1
            )
            VSpacer(dimens.halfBigDim)
            NotificationLabel(NotificationLabelOption.ONLY_FOR_MOSCOW)
            InputText(
                widgetModel = omsInput,
                visualTransformation = MaskVisualTransformation(omsMask),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                singleLine = true
            )
            InputText(
                widgetModel = dateInput,
                visualTransformation = MaskVisualTransformation(DATE_MASK),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                singleLine = true
            )
            VSpacer(height = dimens.bigDim)
            AnimatedVisibility(
                visible = state.isLinked,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                NotificationLabel(NotificationLabelOption.EMIAS_CONNECTED)
            }
        }
    }
}

@Composable
fun NotificationLabel(option: NotificationLabelOption) {
    GetLocalProperties { dimens, _, colors, shapes, styles ->
        val isEmiasConnectedLabel = option == NotificationLabelOption.EMIAS_CONNECTED

        val backgroundColor =
            if (isEmiasConnectedLabel) colors.ghostWhite
            else colors.greenBlue10

        val drawableId =
            if (isEmiasConnectedLabel) R.drawable.ic_complete
            else R.drawable.ic_kremlin

        val textId =
            if (isEmiasConnectedLabel) R.string.emias_notification_account_connected
            else R.string.emias_notification_for_moscow

        val textColor =
            if (isEmiasConnectedLabel) colors.blackBlue
            else colors.greenBlue

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = backgroundColor, shape = shapes.smallButton)
                .padding(dimens.contentPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = drawableId),
                contentDescription = null
            )
            HSpacer(dimens.halfMediumDim)
            Text(
                text = stringResource(id = textId),
                style = styles.title3,
                color = textColor
            )
        }
    }
}

@Preview
@Composable
private fun PreviewEmiasAccountContent() {
    EmiasProfileContent(
        omsInput = InputTextFieldWidgetModel(),
        dateInput = InputTextFieldWidgetModel(),
        state = EmiasProfileViewState(
            oms = "",
            dateBirth = "",
            isLoading = false,
            isLinked = false
        )
    )
}

private const val DATE_MASK = "##.##.####"
private const val OMS_OLD_MASK = "#### #####"
private const val OMS_MASK = "#### #### #### ####"
