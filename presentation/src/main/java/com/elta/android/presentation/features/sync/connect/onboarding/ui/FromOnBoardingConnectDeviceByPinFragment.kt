package com.elta.android.presentation.features.sync.connect.onboarding.ui

import com.elta.android.presentation.features.sync.connect.base.pm.ConnectDevicePm
import com.elta.android.presentation.features.sync.connect.base.ui.ConnectDeviceByPinFragment
import com.elta.android.presentation.features.sync.connect.onboarding.pm.FromOnBoardingConnectDevicePm
import com.nullgr.core.ui.extensions.hide
import com.nullgr.core.ui.extensions.show
import me.dmdev.rxpm.bindTo

class FromOnBoardingConnectDeviceByPinFragment : ConnectDeviceByPinFragment<FromOnBoardingConnectDevicePm>() {

    companion object {
        fun newInstance() = FromOnBoardingConnectDeviceByPinFragment()
    }

    override val classToken: Class<FromOnBoardingConnectDevicePm> =
        FromOnBoardingConnectDevicePm::class.java

    override fun onBindPresentationModel(pm: FromOnBoardingConnectDevicePm) {
        super.onBindPresentationModel(pm)
        pm.connectState.bindTo { state ->
            if (state == ConnectDevicePm.ViewState.SYNC_COMPLETED || state == ConnectDevicePm.ViewState.CONNECTED) {
                binding.toolbar.menuButtonView.hide()
            } else {
                binding.toolbar.menuButtonView.show()
            }
        }
    }
}
