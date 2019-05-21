package com.elta.android.presentation.features.devices.firmware.ui

import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.features.devices.firmware.pm.FirmwarePm
import com.elta.android.presentation.utils.bundle

class FirmwareFragment : BaseFragment<FirmwarePm>() {

    override val screenLayout: Int = R.layout.fragment_update_firmware
    override val classToken: Class<FirmwarePm> = FirmwarePm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    companion object {
        private const val EXTRA_ADDRESS = "extra_address"
        fun newInstance(address: String): FirmwareFragment =
            FirmwareFragment().apply {
                arguments = bundle(EXTRA_ADDRESS to address)
            }
    }
}
