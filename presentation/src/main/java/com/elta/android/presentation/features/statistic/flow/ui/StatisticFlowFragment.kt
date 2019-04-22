package com.elta.android.presentation.features.statistic.flow.ui

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFlowFragment
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.TransparentStatusBarConfigProvider
import com.elta.android.presentation.features.statistic.flow.pm.StatisticFlowPm
import com.elta.android.presentation.utils.applyWindowInsetsForChildrenView
import com.nullgr.core.ui.extensions.hide
import kotlinx.android.synthetic.main.layout_toolbar.*

class StatisticFlowFragment : BaseFlowFragment<StatisticFlowPm>() {

    override val screenLayout: Int = R.layout.fragment_statistic_flow
    override val classToken: Class<StatisticFlowPm> = StatisticFlowPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = TransparentStatusBarConfigProvider
    override val backgroundColor: Int = R.color.pale_gray

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeButtonView.hide()
        menuButtonView.hide()
        toolbarTitleView.setText(R.string.statistic_title)
        toolbarView.setBackgroundColor(ContextCompat.getColor(view.context, R.color.white))
        toolbarView.applyWindowInsetsForChildrenView()
    }

    companion object {
        fun newInstance(): StatisticFlowFragment = StatisticFlowFragment()
    }
}
