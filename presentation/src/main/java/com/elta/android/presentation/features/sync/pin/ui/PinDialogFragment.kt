package com.elta.android.presentation.features.sync.pin.ui

import android.os.Bundle
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseBottomSheetFragment
import com.elta.android.presentation.features.sync.pin.pm.PinDialogPm
import com.jakewharton.rxbinding2.view.clicks
import kotlinx.android.synthetic.main.fragment_enter_pin_dialog.*

class PinDialogFragment : BaseBottomSheetFragment<PinDialogPm>() {

    override val screenLayout: Int = R.layout.fragment_enter_pin_dialog
    override val classToken: Class<PinDialogPm> = PinDialogPm::class.java

    override fun onBindPresentationModel(pm: PinDialogPm) {
        dialogCloseButtonView.clicks().bindTo { dialog.dismiss() }
        dialogActionButtonView.clicks().bindTo(pm.mainAction)
        pm.actionButtonEnabledState.bindTo(dialogActionButtonView::setEnabled)
        pm.closeDialogCommand.bindTo { dialog.dismiss() }
    }

    companion object {
        fun newInstance(): PinDialogFragment {
            return PinDialogFragment().apply {
                arguments = Bundle().apply {
                }
            }
        }
    }
}
