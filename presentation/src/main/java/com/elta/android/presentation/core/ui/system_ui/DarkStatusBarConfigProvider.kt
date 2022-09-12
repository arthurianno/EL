package com.elta.android.presentation.core.ui.system_ui

import com.elta.android.presentation.R

object DarkStatusBarConfigProvider : StatusBarConfigProvider {
    override var drawUnderStatusBar = false
    override val statusBarColor: Int = R.color.color_status_bar_dark
    override val lightStatusBar: Boolean = true
}
