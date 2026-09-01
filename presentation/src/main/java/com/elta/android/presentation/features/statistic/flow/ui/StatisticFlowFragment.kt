package com.elta.android.presentation.features.statistic.flow.ui

import android.os.Bundle
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFlowFragment
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.TransparentStatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentStatisticFlowBinding
import com.elta.android.presentation.features.statistic.flow.pm.StatisticFlowPm
import com.elta.android.presentation.features.statistic.period.ui.Period
import com.elta.android.presentation.features.statistic.report.ui.ReportPeriodChooserFragment
import com.nullgr.core.ui.fragments.showDialog
import me.dmdev.rxpm.bindTo

class StatisticFlowFragment :
    BaseFlowFragment<StatisticFlowPm, FragmentStatisticFlowBinding>(FragmentStatisticFlowBinding::inflate) {

    override val screenLayout: Int = R.layout.fragment_statistic_flow
    override val classToken: Class<StatisticFlowPm> = StatisticFlowPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider =
        TransparentStatusBarConfigProvider
    override val backgroundColor: Int = R.color.pale_gray

    override fun onBindPresentationModel(pm: StatisticFlowPm) {
        super.onBindPresentationModel(pm)
        pm.showReportPeriodChooser.bindTo {
            childFragmentManager.showDialog(ReportPeriodChooserFragment.newInstance())
        }
    }

    fun selectPeriod(period: Period) {
        presentationModel.selectPeriod(period)
    }

    fun openReportChooser() {
        presentationModel.menuAction.consumer.accept(Unit)
    }

    companion object {
        fun newInstance(): StatisticFlowFragment = StatisticFlowFragment()
    }
}
