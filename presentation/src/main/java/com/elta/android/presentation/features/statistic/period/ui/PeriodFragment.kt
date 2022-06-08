package com.elta.android.presentation.features.statistic.period.ui

import android.os.Bundle
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseListFragment
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentStatisticPeriodBinding
import com.elta.android.presentation.features.statistic.period.pm.PeriodPm
import com.elta.android.presentation.utils.bundle

class PeriodFragment :
    BaseListFragment<PeriodPm, FragmentStatisticPeriodBinding>(FragmentStatisticPeriodBinding::inflate) {

    override val screenLayout: Int = R.layout.fragment_statistic_period
    override val classToken: Class<PeriodPm> = PeriodPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider? = null
    override val backgroundColor: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val period = arguments?.getSerializable(EXTRA_PERIOD) as Period
        presentationModel.setPeriod(period)
    }

    override fun onBindPresentationModel(pm: PeriodPm) {
        super.onBindPresentationModel(pm)
        bindProgressDialog(pm)
    }

    companion object {
        private const val EXTRA_PERIOD = "extra_period"
        fun newInstance(period: Period): PeriodFragment {
            return PeriodFragment().apply {
                arguments = bundle(EXTRA_PERIOD to period)
            }
        }
    }
}
