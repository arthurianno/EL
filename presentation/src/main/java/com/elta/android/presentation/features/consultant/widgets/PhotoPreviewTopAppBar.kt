package com.elta.android.presentation.features.consultant.widgets

import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.BaseWidgetModel
import com.elta.android.presentation.core.compose.widgets.buttons.ButtonCircle
import com.elta.android.presentation.features.consultant.model.ConsultantAction
import com.elta.android.presentation.theme.GetLocalProperties

internal class PhotoPreviewTopAppBarWidgetModel : BaseWidgetModel<Unit>() {
    override fun createInitState(): Unit = Unit
}

@Composable
internal fun PhotoPreviewTopAppBar(widgetModel: PhotoPreviewTopAppBarWidgetModel) {
    GetLocalProperties { dimens, _, colors, _, _ ->
        TopAppBar(
            title = {},
            backgroundColor = colors.black,
            elevation = dimens.zero,
            navigationIcon = {
                ButtonCircle(icon = R.drawable.ic_back, tint = colors.white) {
                    widgetModel.sendAction(ConsultantAction.PreviewBackPressure)
                }
            }
        )
    }
}
