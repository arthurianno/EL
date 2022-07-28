package com.elta.android.presentation.features.sync.connect.base.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseListFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentSyncConnectBinding
import com.elta.android.presentation.features.sync.connect.base.pm.ConnectDevicePm
import com.elta.android.presentation.features.sync.control.bindTo
import com.elta.android.presentation.features.sync.control.resolveResults
import com.elta.android.presentation.features.sync.pin.ui.PinDialogFragment
import com.elta.android.presentation.utils.makeSnackBarWithAction
import com.jakewharton.rxbinding2.view.clicks
import com.nullgr.core.ui.extensions.children
import com.nullgr.core.ui.extensions.hide
import com.nullgr.core.ui.extensions.toggleView
import com.nullgr.core.ui.fragments.showDialog
import com.tbruyelle.rxpermissions2.RxPermissions
import me.dmdev.rxpm.bindTo

abstract class ConnectDeviceFragment<T : ConnectDevicePm> :
    BaseListFragment<T, FragmentSyncConnectBinding>(FragmentSyncConnectBinding::inflate) {

    override val screenLayout: Int = R.layout.fragment_sync_connect
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    private val rxPermissions by lazy { RxPermissions(this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.toolbar) {
            homeButtonView.hide()
            menuButtonView.text = getString(R.string.sync_connect_menu_button_text)
        }
    }

    override fun onBindPresentationModel(pm: T) {
        super.onBindPresentationModel(pm)
        bindProgressDialog(pm)
        binding.toolbar.menuButtonView.clicks().bindTo(pm.skipAction)
        binding.layoutSyncStateDeviceFound.actionButtonView.clicks().bindTo(pm.connectDeviceAction)
        binding.layoutSyncStateSyncCompleted.toAppButtonView.clicks().bindTo(pm.toAppAction)
        binding.layoutSyncStateHowToConnect.manualStartButtonView.clicks()
            .bindTo(pm.startScanAction)
        pm.connectDeviceEnabledState.bindTo(binding.layoutSyncStateDeviceFound.actionButtonView::setEnabled)
        pm.mstate.bindTo { state ->
            binding.syncStateContainerView.children().forEach { view ->
                view.toggleView(state.getId() == view.id)
                if (state == ConnectDevicePm.ViewState.SYNC_COMPLETED) {
                    binding.toolbar.homeButtonView.apply {
                        setImageResource(R.drawable.ic_dialog_close)
                        setOnClickListener {
                            pm.toAppAction.consumer.accept(Unit)
                        }
                    }
                } else {
                    binding.toolbar.homeButtonView.setImageResource(R.drawable.ic_back)
                }
            }
        }

        pm.retrySearchControl.bindTo { data, sc ->
            makeSnackBarWithAction(
                binding.root,
                data,
                sc
            )
        }
        pm.retryPinControl.bindTo { data, sc ->
            makeSnackBarWithAction(
                binding.root,
                data,
                sc
            )
        }
        pm.retryConnectControl.bindTo { data, sc ->
            makeSnackBarWithAction(
                binding.root,
                data,
                sc
            )
        }
        pm.retrySyncControl.bindTo { data, sc ->
            makeSnackBarWithAction(
                binding.root,
                data,
                sc
            )
        }

        pm.retryEnableBluetoothControl.bindTo { data, sc ->
            makeSnackBarWithAction(
                binding.root,
                data,
                sc
            )
        }

        pm.btControl.bindTo(compositeUnbind, rxPermissions, this)

        pm.openPinCodeDialogCommand.bindTo {
            childFragmentManager.showDialog(PinDialogFragment.newInstance(it))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        presentationModel.btControl.resolveResults(requestCode, resultCode)
    }

    private fun ConnectDevicePm.ViewState.getId() =
        when (this) {
            ConnectDevicePm.ViewState.SEARCH -> R.id.layoutSyncStateSearch
            ConnectDevicePm.ViewState.FOUND -> R.id.layoutSyncStateDeviceFound
            ConnectDevicePm.ViewState.CONNECTED -> R.id.layoutSyncStateConnected
            ConnectDevicePm.ViewState.HOW_TO_CONNECT -> R.id.layoutSyncStateHowToConnect
            ConnectDevicePm.ViewState.SYNC_COMPLETED -> R.id.layoutSyncStateSyncCompleted
        }
}
