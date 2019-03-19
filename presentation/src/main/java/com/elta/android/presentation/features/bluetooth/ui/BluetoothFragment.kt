package com.elta.android.presentation.features.bluetooth.ui;

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.features.bluetooth.pm.BluetoothPm
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.text
import kotlinx.android.synthetic.main.fragment_bluetooth.*

class BluetoothFragment : BaseFragment<BluetoothPm>() {

    override val screenLayout: Int = R.layout.fragment_bluetooth
    override val classToken: Class<BluetoothPm> = BluetoothPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logView.movementMethod = ScrollingMovementMethod()
    }

    override fun onBindPresentationModel(pm: BluetoothPm) {
        super.onBindPresentationModel(pm)
        writeButtonView.clicks().bindTo(pm.writeAction)
        subscribeButtonView.clicks().bindTo(pm.command)
        pm.commandInputControl.bindTo(commandInputView)
        pm.logState.bindTo(logView.text())
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
