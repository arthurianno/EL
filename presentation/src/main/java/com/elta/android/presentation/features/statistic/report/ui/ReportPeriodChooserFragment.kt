package com.elta.android.presentation.features.statistic.report.ui

import android.os.Bundle
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseBottomSheetFragment
import com.elta.android.presentation.features.statistic.report.pm.ReportPeriodChooserPm
import com.jakewharton.rxbinding2.view.clicks
import kotlinx.android.synthetic.main.fragment_base_settings_dialog.*

class ReportPeriodChooserFragment : BaseBottomSheetFragment<ReportPeriodChooserPm>() {

    override val screenLayout: Int = R.layout.fragment_statistic_report_period_chooser
    override val classToken: Class<ReportPeriodChooserPm> = ReportPeriodChooserPm::class.java

    override fun onBindPresentationModel(pm: ReportPeriodChooserPm) {
        dialogCloseButtonView.clicks().bindTo { dialog.dismiss() }
        dialogActionButtonView.clicks().bindTo(pm.mainAction)
        pm.actionButtonEnabledCommand.bindTo(dialogActionButtonView::setEnabled)
        pm.closeDialogCommand.bindTo { dialog.dismiss() }
    }

    companion object {
        fun newInstance(): ReportPeriodChooserFragment {
            return ReportPeriodChooserFragment().apply {
                arguments = Bundle().apply {
                }
            }
        }
    }
}
