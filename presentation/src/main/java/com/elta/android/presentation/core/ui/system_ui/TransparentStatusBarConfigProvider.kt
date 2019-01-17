package com.elta.android.presentation.core.ui.system_ui

object TransparentStatusBarConfigProvider : StatusBarConfigProvider {

    override val statusBarColor: Int = android.R.color.transparent
    override val lightStatusBar: Boolean = true
}