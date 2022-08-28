package com.elta.android.presentation.features.devices.firmware.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentUpdateFirmwareBinding
import com.elta.android.presentation.features.devices.firmware.pm.FirmwarePm
import com.elta.android.presentation.features.sync.control.bindTo
import com.elta.android.presentation.features.sync.control.resolveResults
import com.elta.android.presentation.utils.bundle
import com.jakewharton.rxbinding2.view.clicks
import com.nullgr.core.ui.extensions.toggleView
import com.tbruyelle.rxpermissions2.RxPermissions
import me.dmdev.rxpm.bindTo

class FirmwareFragment :
    BaseFragment<FirmwarePm, FragmentUpdateFirmwareBinding>(FragmentUpdateFirmwareBinding::inflate) {

    override val screenLayout: Int = R.layout.fragment_update_firmware
    override val classToken: Class<FirmwarePm> = FirmwarePm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    private val rxPermissions by lazy { RxPermissions(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val address = arguments?.getString(EXTRA_ADDRESS)
        address?.let { presentationModel.setDeviceAddress(address) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.homeButtonView.setImageResource(R.drawable.ic_dialog_close)
    }

    override fun onBindPresentationModel(pm: FirmwarePm) {
        super.onBindPresentationModel(pm)
        binding.actionButtonView.clicks().bindTo(pm.buttonAction)
        pm.updateState.bindTo { updateState ->
            with(binding) {
                updateIconView.setImageResource(updateState.icon)
                updateTitleView.text = updateState.title
                updateDescriptionView.text = updateState.description
                updateHintView.toggleView(updateState.hint != null)
                actionButtonView.text = updateState.button
                actionButtonView.toggleView(updateState.button != null)
            }
        }
        pm.btControl.bindTo(compositeDestroy, rxPermissions, this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        presentationModel.btControl.resolveResults(requestCode, resultCode)
    }

    companion object {
        private const val EXTRA_ADDRESS = "extra_address"
        fun newInstance(address: String): FirmwareFragment =
            FirmwareFragment().apply {
                arguments = bundle(EXTRA_ADDRESS to address)
            }
    }
}
