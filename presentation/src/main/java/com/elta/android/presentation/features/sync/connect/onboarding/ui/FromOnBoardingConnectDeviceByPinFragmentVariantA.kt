package com.elta.android.presentation.features.sync.connect.onboarding.ui

import com.elta.android.presentation.features.sync.connect.base.pm.ConnectDevicePmVariantA
import com.elta.android.presentation.features.sync.connect.base.ui.ConnectDeviceByPinFragmentVariantA
import com.elta.android.presentation.features.sync.connect.onboarding.pm.FromOnBoardingConnectDevicePmVariantA
import com.nullgr.core.ui.extensions.hide
import com.nullgr.core.ui.extensions.show
import me.dmdev.rxpm.bindTo

// fixme Variant A : improved_enabling_location
class FromOnBoardingConnectDeviceByPinFragmentVariantA :
    ConnectDeviceByPinFragmentVariantA<FromOnBoardingConnectDevicePmVariantA>() {

    companion object {
        fun newInstance() = FromOnBoardingConnectDeviceByPinFragmentVariantA()
    }

    override val classToken: Class<FromOnBoardingConnectDevicePmVariantA> =
        FromOnBoardingConnectDevicePmVariantA::class.java

    override fun onBindPresentationModel(pm: FromOnBoardingConnectDevicePmVariantA) {
        super.onBindPresentationModel(pm)
        pm.connectState.bindTo { state ->
            if (state == ConnectDevicePmVariantA.ViewState.SYNC_COMPLETED || state == ConnectDevicePmVariantA.ViewState.CONNECTED) {
                binding.toolbar.menuButtonView.hide()
            } else {
                binding.toolbar.menuButtonView.show()
            }
        }
    }
}
