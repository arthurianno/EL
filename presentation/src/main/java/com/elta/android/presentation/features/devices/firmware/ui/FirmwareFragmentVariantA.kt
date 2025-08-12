package com.elta.android.presentation.features.devices.firmware.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.elta.android.domain.features.devices.model.BootModeStatus
import com.elta.android.presentation.R
import com.elta.android.presentation.core.pm.widgets.SnackBarControl
import com.elta.android.presentation.core.ui.dialog.createDialog
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.snackbarview.SnackBarData
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentUpdateFirmwareBinding
import com.elta.android.presentation.features.devices.firmware.pm.FirmwarePmVariantA
import com.elta.android.presentation.features.sync.control.bindTo
import com.elta.android.presentation.features.sync.control.resolveResults
import com.elta.android.presentation.utils.bundle
import com.elta.android.presentation.utils.makeSnackBarWithAction
import com.elta.android.presentation.utils.openSettingsIntent
import com.jakewharton.rxbinding2.view.clicks
import com.nullgr.core.ui.extensions.toggleView
import com.tbruyelle.rxpermissions2.RxPermissions
import me.dmdev.rxpm.bindTo
import me.dmdev.rxpm.widget.bindTo

class FirmwareFragmentVariantA :
    BaseFragment<FirmwarePmVariantA, FragmentUpdateFirmwareBinding>(FragmentUpdateFirmwareBinding::inflate) {

    override val screenLayout: Int = R.layout.fragment_update_firmware
    override val classToken: Class<FirmwarePmVariantA> = FirmwarePmVariantA::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    private val rxPermissions by lazy { RxPermissions(requireActivity()) }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val message = intent.getStringExtra(BootModeStatus.STATUS_NAME_KEY).orEmpty()
            val status = try {
                BootModeStatus.valueOf(message)
            } catch (_: Exception) {
                BootModeStatus.UpdateFailed
            }

            presentationModel.bootModeStatusAction.consumer.accept(status)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intentFilter = IntentFilter(BootModeStatus.ACTION_STATUS_NAME)
        context?.let {
            LocalBroadcastManager.getInstance(it).registerReceiver(receiver, intentFilter)
        }
        val address = arguments?.getString(EXTRA_ADDRESS)
        address?.let { presentationModel.setDeviceAddress(address) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.homeButtonView.setImageResource(R.drawable.ic_dialog_close)
    }

    override fun onBindPresentationModel(pm: FirmwarePmVariantA) {
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
        pm.settingsDialog.bindTo { data, dc -> createDialog(this, dc, data) }
        pm.settingsIsVisible.bindTo {
            if (it) {
                openSettingsIntent(requireContext())
                pm.openSettingsCloseAction.consumer.accept(Unit)
            }
        }

        pm.retryUpdateControl.bindTo { data: SnackBarData, sc: SnackBarControl<SnackBarData> ->
            makeSnackBarWithAction(binding.root, data, sc)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        presentationModel.btControl.resolveResults(requestCode, resultCode)
    }

    companion object {
        private const val EXTRA_ADDRESS = "extra_address"
        fun newInstance(address: String): FirmwareFragmentVariantA =
            FirmwareFragmentVariantA().apply {
                arguments = bundle(EXTRA_ADDRESS to address)
            }
    }
}
