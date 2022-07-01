package com.elta.android.presentation.features.devices.all.ui

import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseListFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentDevicesBinding
import com.elta.android.presentation.features.devices.all.pm.DevicesPm
import com.jakewharton.rxbinding2.view.clicks
import me.dmdev.rxpm.bindTo

class DevicesFragment :
    BaseListFragment<DevicesPm, FragmentDevicesBinding>(FragmentDevicesBinding::inflate) {
    override val screenLayout = R.layout.fragment_devices
    override val classToken = DevicesPm::class.java
    override val statusBarConfigProvider = LightStatusBarConfigProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.menuButtonView.text = getString(R.string.profile_devices_new_device)
    }

    override fun onBindPresentationModel(pm: DevicesPm) {
        super.onBindPresentationModel(pm)
        binding.toolbar.menuButtonView.clicks().bindTo(pm.addNewDeviceAction)
    }

    companion object {
        fun newInstance() = DevicesFragment()
    }
}
