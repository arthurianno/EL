package com.elta.android.presentation.features.sync.pin.ui

import android.app.Dialog
import android.os.Bundle
import android.view.WindowManager
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseBottomSheetFragment
import com.elta.android.presentation.core.ui.keyboardanimator.simple.SimpleKeyboardAnimator
import com.elta.android.presentation.features.sync.pin.pm.PinDialogPm
import com.elta.android.presentation.utils.bundle
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.text
import kotlinx.android.synthetic.main.fragment_enter_pin_dialog.*
import me.dmdev.rxpm.bindTo
import me.dmdev.rxpm.widget.bindTo

class PinDialogFragment : BaseBottomSheetFragment<PinDialogPm>() {

    override val screenLayout: Int = R.layout.fragment_enter_pin_dialog
    override val classToken: Class<PinDialogPm> = PinDialogPm::class.java

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            window?.let { SimpleKeyboardAnimator(it).start() }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getString(EXTRA_NAME)?.let { name ->
            presentationModel.setDeviceName(name)
        }
    }

    override fun onBindPresentationModel(pm: PinDialogPm) {
        dialogCloseButtonView.clicks().subscribe { dialog?.dismiss() }
        dialogActionButtonView.clicks().bindTo(pm.mainAction)
        pm.actionButtonEnabledState.bindTo(dialogActionButtonView::setEnabled)
        pm.closeDialogCommand.bindTo { dialog?.dismiss() }
        pm.pinInputControl.bindTo(pinCodeInputView)
        pm.deviceNameState.bindTo(deviceNameView.text())
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
