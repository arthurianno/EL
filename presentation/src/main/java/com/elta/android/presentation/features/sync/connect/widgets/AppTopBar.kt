package com.elta.android.presentation.features.sync.connect.widgets

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBar
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBarWidgetModel
import com.elta.android.presentation.theme.eltaColors

@Composable
internal fun AppTopBar(
    appTopBarModel: BaseAppTopBarWidgetModel,
    backgroundColor: Color = eltaColors.white,
    iconColor: Color = eltaColors.blackBlue,
    textColor: Color = eltaColors.shadeBlack0,
    @DrawableRes startIcon: Int? = R.drawable.ic_back,
    @StringRes endText: Int? = R.string.sync_connect_type_button_any_difficulties
) {
    BaseAppTopBar(
        widgetModel = appTopBarModel,
        backgroundColor = backgroundColor,
        startIconColor = iconColor,
        textColor = textColor,
        startIcon = startIcon,
        endText = endText
    )
}
