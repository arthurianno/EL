package com.elta.android.presentation.features.sync.connect.base.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.R
import com.elta.android.presentation.core.pm.widgets.SnackBarControl
import com.elta.android.presentation.core.ui.dialog.createDialog
import com.elta.android.presentation.core.ui.fragment.BaseRecyclerViewFragment
import com.elta.android.presentation.core.ui.fragment.addOnBackPressedCallback
import com.elta.android.presentation.core.ui.snackbarview.SnackBarData
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentSyncConnectBinding
import com.elta.android.presentation.features.sync.connect.base.pm.ConnectDevicePm
import com.elta.android.presentation.features.sync.connect.base.ui.adapter.DeviceAdapter
import com.elta.android.presentation.features.sync.control.bindTo
import com.elta.android.presentation.features.sync.control.resolveResults
import com.elta.android.presentation.features.sync.pin.ui.PinDialogFragment
import com.elta.android.presentation.utils.makeSnackBarWithAction
import com.elta.android.presentation.utils.openSettingsIntent
import com.jakewharton.rxbinding2.view.clicks
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.ui.extensions.children
import com.nullgr.core.ui.extensions.hide
import com.nullgr.core.ui.extensions.show
import com.nullgr.core.ui.extensions.toggleView
import com.nullgr.core.ui.fragments.showDialog
import com.tbruyelle.rxpermissions2.RxPermissions
import me.dmdev.rxpm.bindTo
import me.dmdev.rxpm.widget.bindTo
import javax.inject.Inject

abstract class ConnectDeviceByPinFragment<T : ConnectDevicePm> :
    BaseRecyclerViewFragment<T, FragmentSyncConnectBinding>(FragmentSyncConnectBinding::inflate) {

    @Inject
    lateinit var deviceAdapter: DeviceAdapter

    override val adapter: ListAdapter<ListItem, RecyclerView.ViewHolder> by lazy { deviceAdapter }

    override val screenLayout: Int = R.layout.fragment_sync_connect
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    private val rxPermissions by lazy { RxPermissions(this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.toolbar) {
            menuButtonView.text = getString(R.string.sync_connect_menu_button_text)
        }
    }

    override fun onBindPresentationModel(pm: T) {
        super.onBindPresentationModel(pm)
        bindProgressDialog(pm)
        binding.toolbar.menuButtonView.clicks().bindTo(pm.skipAction)
        binding.layoutSyncStateDeviceFound.actionButtonView.clicks().bindTo(pm.connectDeviceAction)
        binding.layoutSyncStateNotFound.actionButtonView.clicks().bindTo(pm.startScanAction)
        binding.layoutSyncStateSyncError.actionButtonView.clicks().bindTo(pm.startSyncAction)
        binding.layoutSyncStateSyncCompleted.toAppButtonView.clicks().bindTo(pm.toAppAction)
        binding.layoutSyncStateHowToConnect.manualStartButtonView.clicks().bindTo(pm.startScanAction)
        binding.toolbar.homeButtonView.clicks().bindTo(pm.backHandleAction)
        pm.connectDeviceEnabledState.bindTo(binding.layoutSyncStateDeviceFound.actionButtonView::setEnabled)
        pm.hideHomeButtonCommand.bindTo { binding.toolbar.homeButtonView.hide() }
        pm.showHomeButtonCommand.bindTo { binding.toolbar.homeButtonView.show() }
        pm.connectState.bindTo { state ->
            binding.syncStateContainerView.children().forEach { view ->
                view.toggleView(state.getId() == view.id)
            }
        }

        pm.retryPinControl.bindTo { data: SnackBarData, sc: SnackBarControl<SnackBarData> ->
            makeSnackBarWithAction(
                binding.root,
                data,
                sc
            )
        }
        pm.retryConnectControl.bindTo { data: SnackBarData, sc: SnackBarControl<SnackBarData> ->
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

        pm.settingsDialog.bindTo { data, dc -> createDialog(this, dc, data) }
        pm.settingsIsVisible.bindTo {
            if (it) {
                openSettingsIntent(requireContext())
                pm.openSettingsCloseAction.consumer.accept(Unit)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        addOnBackPressedCallback {
            presentationModel.backHandleAction.consumer.accept(Unit)
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
            ConnectDevicePm.ViewState.SYNC_ERROR -> R.id.layoutSyncStateSyncError
            ConnectDevicePm.ViewState.NOT_FOUND -> R.id.layoutSyncStateNotFound
        }
}
