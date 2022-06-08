package com.elta.android.presentation.features.devices.info.ui

import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.dialog.createDialog
import com.elta.android.presentation.core.ui.fragment.BaseListFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentDeviceInfoBinding
import com.elta.android.presentation.features.devices.info.pm.DeviceInfoPm
import com.elta.android.presentation.utils.bundle
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.text
import me.dmdev.rxpm.bindTo
import me.dmdev.rxpm.widget.bindTo

class DeviceInfoFragment :
    BaseListFragment<DeviceInfoPm, FragmentDeviceInfoBinding>(FragmentDeviceInfoBinding::inflate) {

    override val screenLayout: Int = R.layout.fragment_device_info
    override val classToken: Class<DeviceInfoPm> = DeviceInfoPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val name = arguments?.getString(DEVICE_NAME).orEmpty()
        val address = arguments?.getString(DEVICE_ADDRESS).orEmpty()
        presentationModel.setDeviceData(name, address)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.menuButtonView.text = getString(R.string.profile_device_info_delete)
    }

    override fun onBindPresentationModel(pm: DeviceInfoPm) {
        super.onBindPresentationModel(pm)
        bindProgressDialog(pm)
        binding.toolbar.menuButtonView.clicks().bindTo(pm.deleteDeviceAction)
        binding.checkUpdateButtonView.clicks().bindTo(pm.checkUpdateAction)
        pm.nameDeviceState.bindTo(binding.titleTextView.text())
        pm.descriptionAddressState.bindTo(binding.descriptionTextView.text())
        pm.deleteDeviceDialogControl.bindTo { data, dc -> createDialog(this, dc, data) }
    }

    companion object {
        fun newInstance(name: String, address: String) = DeviceInfoFragment().apply {
            arguments = bundle(
                DEVICE_NAME to name,
                DEVICE_ADDRESS to address
            )
        }

        private const val DEVICE_NAME = "device_name"
        private const val DEVICE_ADDRESS = "device_address"
    }
}
