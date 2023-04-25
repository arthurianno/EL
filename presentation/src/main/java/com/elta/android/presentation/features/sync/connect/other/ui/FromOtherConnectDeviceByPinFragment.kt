package com.elta.android.presentation.features.sync.connect.other.ui

import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.features.sync.connect.base.ui.ConnectDeviceByPinFragment
import com.elta.android.presentation.features.sync.connect.other.pm.FromOtherConnectDevicePm
import com.nullgr.core.ui.extensions.hide
import com.nullgr.core.ui.extensions.show

class FromOtherConnectDeviceByPinFragment : ConnectDeviceByPinFragment<FromOtherConnectDevicePm>() {

    override val classToken: Class<FromOtherConnectDevicePm> = FromOtherConnectDevicePm::class.java

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.toolbar) {
            homeButtonView.setImageResource(R.drawable.ic_back)
            homeButtonView.show()
            menuButtonView.hide()
        }
    }

    companion object {
        fun newInstance() = FromOtherConnectDeviceByPinFragment()
    }
}
