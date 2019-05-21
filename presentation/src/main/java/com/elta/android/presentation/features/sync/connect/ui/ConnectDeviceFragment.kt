package com.elta.android.presentation.features.sync.connect.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseListFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.features.sync.connect.pm.ConnectDevicePm
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
import kotlinx.android.synthetic.main.fragment_sync_connect.*
import kotlinx.android.synthetic.main.layout_sync_state_device_found.*
import kotlinx.android.synthetic.main.layout_sync_state_sync_completed.*
import kotlinx.android.synthetic.main.layout_toolbar.*

class ConnectDeviceFragment : BaseListFragment<ConnectDevicePm>() {

    override val screenLayout: Int = R.layout.fragment_sync_connect
    override val classToken: Class<ConnectDevicePm> = ConnectDevicePm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    private val rxPermissions by lazy { RxPermissions(this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeButtonView.hide()
        menuButtonView.text = getString(R.string.sync_connect_menu_button_text)
    }

    override fun onBindPresentationModel(pm: ConnectDevicePm) {
        super.onBindPresentationModel(pm)
        bindProgressDialog(pm)
        menuButtonView.clicks().bindTo(pm.skipAction)
        actionButtonView.clicks().bindTo(pm.connectDeviceAction)
        toAppButtonView.clicks().bindTo(pm.toAppAction)
        pm.connectDeviceEnabledState.bindTo(actionButtonView::setEnabled)
        pm.state.bindTo { state ->
            syncStateContainerView.children().forEach { view ->
                view.toggleView(state.getId() == view.id)
            }
        }

        pm.retrySearchControl.bindTo { data, sc -> makeSnackBarWithAction(checkNotNull(view), data, sc) }
        pm.retryPinControl.bindTo { data, sc -> makeSnackBarWithAction(checkNotNull(view), data, sc) }
        pm.retryConnectControl.bindTo { data, sc -> makeSnackBarWithAction(checkNotNull(view), data, sc) }
        pm.retrySyncControl.bindTo { data, sc -> makeSnackBarWithAction(checkNotNull(view), data, sc) }

        pm.btControl.bindTo(compositeUnbind, rxPermissions, this)

        pm.openPinCodeDialogCommand.bindTo {
            childFragmentManager.showDialog(PinDialogFragment.newInstance(it))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        presentationModel.btControl.resolveResults(requestCode, resultCode)
    }

    private inline fun ConnectDevicePm.ViewState.getId() =
        when (this) {
            ConnectDevicePm.ViewState.SEARCH -> R.id.stateConnectView
            ConnectDevicePm.ViewState.FOUND -> R.id.stateDeviceFoundView
            ConnectDevicePm.ViewState.CONNECTED -> R.id.stateConnectedView
            ConnectDevicePm.ViewState.SYNC_COMPLETED -> R.id.stateSyncCompletedView
        }

    companion object {
        fun newInstance(): ConnectDeviceFragment = ConnectDeviceFragment()
    }
}
