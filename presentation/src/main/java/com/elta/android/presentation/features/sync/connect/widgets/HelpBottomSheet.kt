package com.elta.android.presentation.features.sync.connect.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.widgets.VSpacer
import com.elta.android.presentation.core.compose.widgets.VSpacerMedium
import com.elta.android.presentation.core.compose.widgets.buttons.ButtonCircle
import com.elta.android.presentation.core.compose.widgets.buttons.DownButton
import com.elta.android.presentation.core.compose.widgets.buttons.DownButtonWidgetModel
import com.elta.android.presentation.features.sync.connect.model.ConnectAction
import com.elta.android.presentation.theme.GetLocalProperties

@Composable
internal fun HelpBottomSheet(
    downButtonModel: DownButtonWidgetModel,
    connectAction: Action = ConnectAction.ConnectByPin,
    closeOnClick: () -> Unit
) {
    GetLocalProperties { dimens, _, colors, _, types ->
        VSpacer(height = dimens.sheetTopPadding)
        Row(
            modifier = Modifier.padding(start = dimens.contentPadding),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Text(
                text = stringResource(id = R.string.sync_connection_help_sheet_title),
                style = types.h2,
                modifier = Modifier.weight(1f)
            )
            ButtonCircle(
                icon = R.drawable.ic_dialog_close_profile,
                onClick = closeOnClick,
                contentDescriptionId = R.string.content_description_close_button
            )
        }
        VSpacerMedium()
        Text(
            text = stringResource(id = R.string.sync_connection_help_sheet_text),
            color = colors.shadeBlack0,
            modifier = Modifier.padding(horizontal = dimens.contentPadding)
        )
        VSpacer(height = dimens.bigDim)
        DownButton(
            widgetModel = downButtonModel,
            onClickAction = connectAction
        )
    }
}
