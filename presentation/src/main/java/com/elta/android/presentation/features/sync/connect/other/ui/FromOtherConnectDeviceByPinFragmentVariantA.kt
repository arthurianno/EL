package com.elta.android.presentation.features.sync.connect.other.ui

import com.elta.android.presentation.features.sync.connect.base.ui.ConnectDeviceByPinFragmentVariantA
import com.elta.android.presentation.features.sync.connect.other.pm.FromOtherConnectDevicePmVariantA

class FromOtherConnectDeviceByPinFragmentVariantA : ConnectDeviceByPinFragmentVariantA<FromOtherConnectDevicePmVariantA>() {
    override val classToken: Class<FromOtherConnectDevicePmVariantA> = FromOtherConnectDevicePmVariantA::class.java

    companion object {
        fun newInstance() = FromOtherConnectDeviceByPinFragmentVariantA()
    }
}
