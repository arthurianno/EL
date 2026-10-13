package com.elta.android.presentation.features.sync.connect.onboarding.ui

import com.elta.android.presentation.features.sync.connect.base.ui.ConnectDeviceByPinFragmentVariantA
import com.elta.android.presentation.features.sync.connect.onboarding.pm.FromOnBoardingConnectDevicePmVariantA

class FromOnBoardingConnectDeviceByPinFragmentVariantA : ConnectDeviceByPinFragmentVariantA<FromOnBoardingConnectDevicePmVariantA>() {
    override val classToken: Class<FromOnBoardingConnectDevicePmVariantA> = FromOnBoardingConnectDevicePmVariantA::class.java

    companion object {
        fun newInstance() = FromOnBoardingConnectDeviceByPinFragmentVariantA()
    }
}
