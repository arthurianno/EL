package com.elta.android.presentation.features.sync.connect.base.ui

import android.content.Intent
import com.elta.android.presentation.core.ui.dialog.createDialog
import com.elta.android.presentation.features.sync.connect.pin.PinConnectionFragment
import com.elta.android.presentation.features.sync.connect.base.pm.ConnectDevicePmVariantA
import com.elta.android.presentation.features.sync.control.bindTo
import com.elta.android.presentation.features.sync.control.resolveResults
import com.elta.android.presentation.utils.openSettingsIntent
import com.tbruyelle.rxpermissions2.RxPermissions
import me.dmdev.rxpm.bindTo
import me.dmdev.rxpm.widget.bindTo

abstract class ConnectDeviceByPinFragmentVariantA<T : ConnectDevicePmVariantA> : PinConnectionFragment<T>() {

    private val rxPermissions: RxPermissions by lazy { RxPermissions(requireActivity()) }

    override fun onBindPresentationModel(pm: T) {
        super.onBindPresentationModel(pm)
        // Откладываем инициализацию RxPermissions чтобы избежать конфликта с FragmentManager
        view?.post {
            if (isAdded && !isStateSaved) {
                pm.btControl.bindTo(compositeUnbind, rxPermissions, this)
            }
        }

        pm.settingsDialog.bindTo { data, dc -> createDialog(this, dc, data) }
        pm.settingsIsVisible.bindTo {
            if (it) {
                openSettingsIntent(requireContext())
                pm.openSettingsCloseAction.consumer.accept(Unit)
            }
        }
        pm.deviceAlreadyConnectedDialog.bindTo { data, dc -> createDialog(this, dc, data) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        presentationModel.btControl.resolveResults(requestCode, resultCode)
    }

}
