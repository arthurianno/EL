package com.elta.android.presentation.core.ui.system_ui

import com.elta.android.presentation.R

object TransparentLightStatusBarConfigProvider : StatusBarConfigProvider {
    override var drawUnderStatusBar = true
    override val statusBarColor: Int = R.color.color_status_bar_transparent
    override val lightStatusBar: Boolean = true
}
