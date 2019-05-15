package com.elta.android.presentation.features.devices.info.ui

import android.os.Bundle
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseListFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.features.devices.info.pm.DeviceInfoPm
import com.jakewharton.rxbinding2.view.clicks
import kotlinx.android.synthetic.main.layout_toolbar.*

class DeviceInfoFragment : BaseListFragment<DeviceInfoPm>() {

    override val screenLayout: Int = R.layout.fragment_device_info
    override val classToken: Class<DeviceInfoPm> = DeviceInfoPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        menuButtonView.text = getString(R.string.profile_device_info_delete)
    }

    override fun onBindPresentationModel(pm: DeviceInfoPm) {
        super.onBindPresentationModel(pm)
        bindProgressDialog(pm)
        menuButtonView.clicks().bindTo(pm.deleteDeviceAction)

        pm.deleteDeviceDialogControl.bindTo { data, dc ->
            MaterialDialog.Builder(checkNotNull(activity))
                .cancelable(false)
                .title(data.title)
                .content(data.message)
                .negativeText(data.negative)
                .positiveText(data.positive)
                .onPositive { _, _ -> dc.sendResult(DeviceInfoPm.DialogResult.POSITIVE) }
                .onNegative { _, _ -> dc.sendResult(DeviceInfoPm.DialogResult.NEGATIVE) }
                .build()
        }
    }

    companion object {
        fun newInstance() = DeviceInfoFragment()
    }
}