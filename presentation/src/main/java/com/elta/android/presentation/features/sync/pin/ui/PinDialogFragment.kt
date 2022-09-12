package com.elta.android.presentation.features.sync.pin.ui

import android.app.Dialog
import android.os.Bundle
import android.view.WindowManager
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseBottomSheetFragment
import com.elta.android.presentation.databinding.FragmentEnterPinDialogBinding
import com.elta.android.presentation.features.sync.pin.pm.PinDialogPm
import com.elta.android.presentation.utils.bundle
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.text
import me.dmdev.rxpm.bindTo
import me.dmdev.rxpm.widget.bindTo

class PinDialogFragment : BaseBottomSheetFragment<PinDialogPm, FragmentEnterPinDialogBinding>(
    FragmentEnterPinDialogBinding::inflate
) {

    override val screenLayout: Int = R.layout.fragment_enter_pin_dialog
    override val classToken: Class<PinDialogPm> = PinDialogPm::class.java

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getString(EXTRA_NAME)?.let { name ->
            presentationModel.setDeviceName(name)
        }
    }

    override fun onBindPresentationModel(pm: PinDialogPm) {
        binding.dialogCloseButtonView.clicks().subscribe { dialog?.dismiss() }
        binding.dialogActionButtonView.clicks().bindTo(pm.mainAction)
        pm.actionButtonEnabledState.bindTo(binding.dialogActionButtonView::setEnabled)
        pm.closeDialogCommand.bindTo { dialog?.dismiss() }
        pm.pinInputControl.bindTo(binding.pinCodeInputView)
        pm.deviceNameState.bindTo(binding.deviceNameView.text())
    }

    companion object {
        private const val EXTRA_NAME = "extra_name"
        fun newInstance(name: String): PinDialogFragment {
            return PinDialogFragment().apply {
                arguments = bundle(EXTRA_NAME to name)
            }
        }
    }
}
