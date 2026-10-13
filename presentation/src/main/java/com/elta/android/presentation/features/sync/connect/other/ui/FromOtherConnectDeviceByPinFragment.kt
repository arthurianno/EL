package com.elta.android.presentation.features.sync.connect.other.ui

import com.elta.android.presentation.features.sync.connect.base.ui.ConnectDeviceByPinFragment
import com.elta.android.presentation.features.sync.connect.other.pm.FromOtherConnectDevicePm

class FromOtherConnectDeviceByPinFragment : ConnectDeviceByPinFragment<FromOtherConnectDevicePm>() {
    override val classToken: Class<FromOtherConnectDevicePm> = FromOtherConnectDevicePm::class.java

    companion object {
        fun newInstance() = FromOtherConnectDeviceByPinFragment()
    }
}
