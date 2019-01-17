package com.elta.android.presentation.core.ui.system_ui

import com.elta.android.presentation.R

object LightStatusBarConfigProvider : StatusBarConfigProvider {

    override val statusBarColor: Int = R.color.color_status_bar_light
    override val lightStatusBar: Boolean = false
}