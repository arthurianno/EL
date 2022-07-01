package com.elta.android.presentation.features.statistic.flow.ui

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFlowFragment
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.TransparentStatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentStatisticFlowBinding
import com.elta.android.presentation.features.statistic.flow.pm.StatisticFlowPm
import com.elta.android.presentation.features.statistic.report.ui.ReportPeriodChooserFragment
import com.elta.android.presentation.utils.applyWindowInsetsForChildrenView
import com.jakewharton.rxbinding2.view.clicks
import com.nullgr.core.ui.extensions.hide
import com.nullgr.core.ui.fragments.showDialog
import me.dmdev.rxpm.bindTo

class StatisticFlowFragment :
    BaseFlowFragment<StatisticFlowPm, FragmentStatisticFlowBinding>(FragmentStatisticFlowBinding::inflate) {

    override val screenLayout: Int = R.layout.fragment_statistic_flow
    override val classToken: Class<StatisticFlowPm> = StatisticFlowPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider =
        TransparentStatusBarConfigProvider
    override val backgroundColor: Int = R.color.pale_gray

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.toolbar) {
            homeButtonView.hide()
            menuButtonView.setText(R.string.statistic_menu_button)
            toolbarTitleView.setText(R.string.statistic_title)
            toolbarView.setBackgroundColor(ContextCompat.getColor(view.context, R.color.white))
            toolbarView.applyWindowInsetsForChildrenView()
        }
    }

    override fun onBindPresentationModel(pm: StatisticFlowPm) {
        super.onBindPresentationModel(pm)
        binding.periodTabsView.tabClicks().bindTo(pm.periodSelectedAction)
        pm.selectedPeriodIdState.bindTo(binding.periodTabsView.selection())
        binding.toolbar.menuButtonView.clicks().bindTo(pm.menuAction)
        pm.showReportPeriodChooser.bindTo {
            childFragmentManager.showDialog(ReportPeriodChooserFragment.newInstance())
        }
    }

    companion object {
        fun newInstance(): StatisticFlowFragment = StatisticFlowFragment()
    }
}
