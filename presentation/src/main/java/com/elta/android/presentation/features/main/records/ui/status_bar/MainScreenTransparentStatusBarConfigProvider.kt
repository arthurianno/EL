package com.elta.android.presentation.features.main.records.ui.status_bar

import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider

object MainScreenTransparentStatusBarConfigProvider : StatusBarConfigProvider {
    override val statusBarColor = R.color.color_status_bar_transparent
    override val lightStatusBar = true
    override var drawUnderStatusBar = true
}
