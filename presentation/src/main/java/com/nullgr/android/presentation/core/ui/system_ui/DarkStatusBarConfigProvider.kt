package com.nullgr.android.presentation.core.ui.system_ui

import com.nullgr.android.presentation.R

object DarkStatusBarConfigProvider : StatusBarConfigProvider {

    override val statusBarColor: Int = R.color.color_status_bar_dark
    override val lightStatusBar: Boolean = true
}