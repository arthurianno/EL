package com.elta.android.presentation.features.devices.firmware.ui

import android.os.Bundle
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.features.devices.firmware.pm.FirmwarePm
import com.elta.android.presentation.utils.bundle
import com.jakewharton.rxbinding2.view.clicks
import com.nullgr.core.ui.extensions.toggleView
import kotlinx.android.synthetic.main.fragment_update_firmware.*

class FirmwareFragment : BaseFragment<FirmwarePm>() {

    override val screenLayout: Int = R.layout.fragment_update_firmware
    override val classToken: Class<FirmwarePm> = FirmwarePm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val address = arguments?.getString(EXTRA_ADDRESS)
        address?.let { presentationModel.setDeviceAddress(address) }
    }

    override fun onBindPresentationModel(pm: FirmwarePm) {
        super.onBindPresentationModel(pm)
        actionButtonView.clicks().bindTo(pm.buttonAction)
        pm.updateState.bindTo {
            with(it) {
                updateTitleView.text = title
                updateVersionView.text = version
                updateHintView.toggleView(hint != null)
                actionButtonView.text = button
                actionButtonView.toggleView(button != null)
            }
        }
    }

    companion object {
        private const val EXTRA_ADDRESS = "extra_address"
        fun newInstance(address: String): FirmwareFragment =
            FirmwareFragment().apply {
                arguments = bundle(EXTRA_ADDRESS to address)
            }
    }
}
