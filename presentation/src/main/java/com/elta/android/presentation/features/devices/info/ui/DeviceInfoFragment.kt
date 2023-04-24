package com.elta.android.presentation.features.devices.info.ui

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.dialog.createDialog
import com.elta.android.presentation.core.ui.fragment.BaseRecyclerViewFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentDeviceInfoBinding
import com.elta.android.presentation.features.devices.info.pm.DeviceInfoPm
import com.elta.android.presentation.features.devices.info.ui.adapter.DeviceInfoAdapter
import com.elta.android.presentation.utils.bundle
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.text
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.intents.launchForResult
import me.dmdev.rxpm.bindTo
import me.dmdev.rxpm.widget.bindTo
import javax.inject.Inject

private const val DEVICE_NAME = "device_name"
private const val DEVICE_ADDRESS = "device_address"
private const val REQUEST_CODE_ENABLE_BLUETOOTH = 146

class DeviceInfoFragment :
    BaseRecyclerViewFragment<DeviceInfoPm, FragmentDeviceInfoBinding>(FragmentDeviceInfoBinding::inflate) {

    companion object {
        fun newInstance(name: String, address: String) = DeviceInfoFragment().apply {
            arguments = bundle(
                DEVICE_NAME to name,
                DEVICE_ADDRESS to address
            )
        }
    }

    @Inject
    lateinit var deviceInfoAdapter: DeviceInfoAdapter

    override val screenLayout: Int = R.layout.fragment_device_info
    override val classToken: Class<DeviceInfoPm> = DeviceInfoPm::class.java
    override val adapter: ListAdapter<ListItem, RecyclerView.ViewHolder> by lazy { deviceInfoAdapter }

    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val name = arguments?.getString(DEVICE_NAME).orEmpty()
        val address = arguments?.getString(DEVICE_ADDRESS).orEmpty()
        presentationModel.setDeviceData(name, address)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.menuButtonView.text = getString(R.string.profile_device_info_delete)
    }

    override fun onBindPresentationModel(pm: DeviceInfoPm) {
        super.onBindPresentationModel(pm)
        bindProgressDialog(pm)
        binding.toolbar.menuButtonView.clicks().bindTo(pm.deleteDeviceAction)
        binding.checkUpdateButtonView.clicks().bindTo(pm.checkUpdateAction)
        pm.nameDeviceState.bindTo(binding.titleTextView.text())
        pm.descriptionAddressState.bindTo(binding.descriptionTextView.text())
        pm.deleteDeviceDialogControl.bindTo { data, dc -> createDialog(this, dc, data) }
        pm.requestEnableBluetoothCommand.bindTo {
            Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                .launchForResult(requireActivity(), REQUEST_CODE_ENABLE_BLUETOOTH)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ENABLE_BLUETOOTH && resultCode == Activity.RESULT_OK) {
            presentationModel.bluetoothEnabledAction.consumer.accept(Unit)
        }
    }
}
