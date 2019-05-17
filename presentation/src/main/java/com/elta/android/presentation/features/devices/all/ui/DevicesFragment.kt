package com.elta.android.presentation.features.devices.all.ui

import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseListFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.features.devices.all.pm.DevicesPm
import com.nullgr.core.ui.decor.DividerItemDecoration
import kotlinx.android.synthetic.main.layout_toolbar.*

class DevicesFragment : BaseListFragment<DevicesPm>() {
    override val screenLayout = R.layout.fragment_devices
    override val classToken = DevicesPm::class.java
    override val statusBarConfigProvider = LightStatusBarConfigProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        menuButtonView.text = getString(R.string.profile_devices_new_device)
    }

    companion object {
        fun newInstance() = DevicesFragment()
    }
}