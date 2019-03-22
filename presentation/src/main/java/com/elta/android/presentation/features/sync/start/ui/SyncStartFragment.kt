package com.elta.android.presentation.features.sync.start.ui

import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.features.sync.start.pm.SyncStartPm
import com.jakewharton.rxbinding2.view.clicks
import com.nullgr.core.ui.extensions.hide
import kotlinx.android.synthetic.main.fragment_sync_start.*
import kotlinx.android.synthetic.main.layout_auth_toolbar.*

class SyncStartFragment : BaseFragment<SyncStartPm>() {

    override val screenLayout: Int = R.layout.fragment_sync_start
    override val classToken: Class<SyncStartPm> = SyncStartPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeButtonView.hide()
        menuButtonView.text = getString(R.string.shops_start_menu_button_text)
    }

    override fun onBindPresentationModel(pm: SyncStartPm) {
        super.onBindPresentationModel(pm)
        menuButtonView.clicks().bindTo(pm.skipAction)
        actionButtonView.clicks().bindTo(pm.mainAction)
    }

    companion object {
        fun newInstance(): SyncStartFragment {
            return SyncStartFragment().apply {
                arguments = Bundle().apply {
                }
            }
        }
    }
}
