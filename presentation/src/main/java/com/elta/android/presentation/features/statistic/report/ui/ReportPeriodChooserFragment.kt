package com.elta.android.presentation.features.statistic.report.ui

import android.os.Bundle
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.features.statistic.report.pm.ReportPeriodChooserPm

class ReportPeriodChooserFragment : BaseFragment<ReportPeriodChooserPm>() {

    override val screenLayout: Int = R.layout.fragment_statistic_report_period_chooser
    override val classToken: Class<ReportPeriodChooserPm> = ReportPeriodChooserPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    companion object {
        fun newInstance(): ReportPeriodChooserFragment {
            return ReportPeriodChooserFragment().apply {
                arguments = Bundle().apply {
                }
            }
        }
    }
}
