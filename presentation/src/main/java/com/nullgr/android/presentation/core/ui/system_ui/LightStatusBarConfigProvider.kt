package com.nullgr.android.presentation.core.ui.system_ui

import com.nullgr.android.presentation.R

object LightStatusBarConfigProvider : StatusBarConfigProvider {

    override val statusBarColor: Int = R.color.color_status_bar_light
    override val lightStatusBar: Boolean = false
}