package com.elta.android.presentation.features.sync.connect.onboarding.ui

import com.elta.android.presentation.features.sync.connect.base.pm.ConnectDevicePm
import com.elta.android.presentation.features.sync.connect.base.ui.ConnectDeviceFragment
import com.elta.android.presentation.features.sync.connect.onboarding.pm.FromOnBoardingConnectDevicePm
import com.nullgr.core.ui.extensions.children
import com.nullgr.core.ui.extensions.hide
import com.nullgr.core.ui.extensions.show
import com.nullgr.core.ui.extensions.toggleView
import me.dmdev.rxpm.bindTo

class FromOnBoardingConnectDeviceFragment : ConnectDeviceFragment<FromOnBoardingConnectDevicePm>() {

    companion object {
        fun newInstance() = FromOnBoardingConnectDeviceFragment()
    }

    override val classToken: Class<FromOnBoardingConnectDevicePm> =
        FromOnBoardingConnectDevicePm::class.java

    override fun onBindPresentationModel(pm: FromOnBoardingConnectDevicePm) {
        super.onBindPresentationModel(pm)
        pm.mstate.bindTo { state ->
            binding.syncStateContainerView.children().forEach { view ->
                view.toggleView(state.getId() == view.id)
                if (state == ConnectDevicePm.ViewState.SYNC_COMPLETED || state == ConnectDevicePm.ViewState.CONNECTED) {
                    binding.toolbar.menuButtonView.hide()
                } else {
                    binding.toolbar.menuButtonView.show()
                }
            }
        }
    }
}
