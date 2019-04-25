package com.elta.android.presentation.features.sync.connect.ui

import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.features.sync.connect.pm.ConnectDevicePm
import com.jakewharton.rxbinding2.view.clicks
import com.nullgr.core.ui.extensions.hide
import kotlinx.android.synthetic.main.fragment_sync_connect.*
import kotlinx.android.synthetic.main.layout_toolbar.*

class ConnectDeviceFragment : BaseFragment<ConnectDevicePm>() {

    override val screenLayout: Int = R.layout.fragment_sync_connect
    override val classToken: Class<ConnectDevicePm> = ConnectDevicePm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeButtonView.hide()
        menuButtonView.text = getString(R.string.sync_connect_menu_button_text)
    }

    override fun onBindPresentationModel(pm: ConnectDevicePm) {
        super.onBindPresentationModel(pm)
        menuButtonView.clicks().bindTo(pm.skipAction)
    }

    companion object {
        fun newInstance(): ConnectDeviceFragment = ConnectDeviceFragment()
    }
}
