package com.elta.android.presentation.features.bluetooth.ui

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseListFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.features.bluetooth.pm.BluetoothPm
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.text
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.fragment_bluetooth.*

class BluetoothFragment : BaseListFragment<BluetoothPm>() {

    override val screenLayout: Int = R.layout.fragment_bluetooth
    override val classToken: Class<BluetoothPm> = BluetoothPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    private val rxPermissions by lazy { RxPermissions(this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logView.movementMethod = ScrollingMovementMethod()
    }

    override fun onBindPresentationModel(pm: BluetoothPm) {
        super.onBindPresentationModel(pm)
        writeButtonView.clicks().bindTo(pm.writeAction)
        connectButtonView.clicks().bindTo(pm.connectAction)
        dfuButtonView.clicks().bindTo(pm.dfuAction)
        pm.commandInputControl.bindTo(commandInputView)
        pm.logState.bindTo(logView.text())
        pm.requestPermissionsCommand.bindTo {
            rxPermissions.request(android.Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribe()
        }
    }

    companion object {
        fun newInstance(): BluetoothFragment {
            return BluetoothFragment().apply {
                arguments = Bundle().apply {
                }
            }
        }
    }
}
