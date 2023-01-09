package com.elta.android.presentation.features.consultant.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.BaseWidgetModel
import com.elta.android.presentation.core.compose.widgets.HSpacerMedium
import com.elta.android.presentation.core.compose.widgets.buttons.RoundedButton
import com.elta.android.presentation.features.consultant.model.ConsultantAction
import com.elta.android.presentation.theme.GetLocalProperties

internal class PhotoPreviewBottomAppBarWidgetModel : BaseWidgetModel<Unit>() {
    override fun createInitState() = Unit
}

@Composable
internal fun PhotoPreviewBottomAppBar(widgetModel: PhotoPreviewBottomAppBarWidgetModel) {
    GetLocalProperties { dimens, _, colors, _, _ ->
        Row(
            Modifier.fillMaxWidth().padding(dimens.photoPreviewBottomBarContentPadding),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.consultant_send_photo),
                color = colors.white
            )
            HSpacerMedium()
            RoundedButton(
                icon = R.drawable.ic_send,
                background = colors.gGreenB,
                border = colors.gGreenB,
                size = dimens.previewSendButtonSize,
                onClick = { widgetModel.sendAction(ConsultantAction.PreviewSendClick) }
            )
        }
    }
}
