package com.elta.android.presentation.features.sync.connect.other.ui

import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.features.sync.connect.base.ui.ConnectDeviceByPinFragmentVariantA
import com.elta.android.presentation.features.sync.connect.other.pm.FromOtherConnectDevicePmVariantA
import com.nullgr.core.ui.extensions.hide
import com.nullgr.core.ui.extensions.show

class FromOtherConnectDeviceByPinFragmentVariantA : ConnectDeviceByPinFragmentVariantA<FromOtherConnectDevicePmVariantA>() {

    override val classToken: Class<FromOtherConnectDevicePmVariantA> = FromOtherConnectDevicePmVariantA::class.java

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.toolbar) {
            homeButtonView.setImageResource(R.drawable.ic_back)
            homeButtonView.show()
            menuButtonView.hide()
        }
    }

    override fun onBindPresentationModel(pm: FromOtherConnectDevicePmVariantA) {
        super.onBindPresentationModel(pm)
    }

    companion object {
        fun newInstance() = FromOtherConnectDeviceByPinFragmentVariantA()
    }
}
