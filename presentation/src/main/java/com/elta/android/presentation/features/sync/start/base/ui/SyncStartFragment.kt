package com.elta.android.presentation.features.sync.start.base.ui

import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.features.sync.start.base.pm.SyncStartPm
import com.jakewharton.rxbinding2.view.clicks
import com.nullgr.core.ui.extensions.hide
import kotlinx.android.synthetic.main.fragment_sync_start.*
import kotlinx.android.synthetic.main.layout_toolbar.*

abstract class SyncStartFragment<T : SyncStartPm> : BaseFragment<T>() {

    override val screenLayout: Int = R.layout.fragment_sync_start
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeButtonView.hide()
        menuButtonView.text = getString(R.string.sync_start_menu_button_text)
    }

    override fun onBindPresentationModel(pm: T) {
        super.onBindPresentationModel(pm)
        menuButtonView.clicks().bindTo(pm.skipAction)
        actionButtonView.clicks().bindTo(pm.mainAction)
    }
}
