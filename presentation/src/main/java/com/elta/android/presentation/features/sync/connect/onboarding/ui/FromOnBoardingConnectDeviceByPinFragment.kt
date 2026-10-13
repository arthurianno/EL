package com.elta.android.presentation.features.sync.connect.onboarding.ui

import com.elta.android.presentation.features.sync.connect.base.ui.ConnectDeviceByPinFragment
import com.elta.android.presentation.features.sync.connect.onboarding.pm.FromOnBoardingConnectDevicePm

class FromOnBoardingConnectDeviceByPinFragment : ConnectDeviceByPinFragment<FromOnBoardingConnectDevicePm>() {
    override val classToken: Class<FromOnBoardingConnectDevicePm> = FromOnBoardingConnectDevicePm::class.java

    companion object {
        fun newInstance() = FromOnBoardingConnectDeviceByPinFragment()
    }
}
