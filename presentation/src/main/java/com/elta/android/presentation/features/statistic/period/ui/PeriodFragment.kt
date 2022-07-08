package com.elta.android.presentation.features.statistic.period.ui

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.core.ui.fragment.BaseRecyclerViewFragment
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentStatisticPeriodBinding
import com.elta.android.presentation.features.statistic.period.pm.PeriodPm
import com.elta.android.presentation.features.statistic.period.ui.adapter.PeriodAdapter
import com.elta.android.presentation.utils.bundle
import com.elta.android.presentation.widgets.charts.statistics.listeners.OnStatisticsDateChangedListener
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.rx.RxBus
import me.dmdev.rxpm.bindTo
import org.threeten.bp.LocalDate
import javax.inject.Inject

class PeriodFragment :
    BaseRecyclerViewFragment<PeriodPm, FragmentStatisticPeriodBinding>(
        FragmentStatisticPeriodBinding::inflate
    ) {

    @Inject
    lateinit var periodAdapter: PeriodAdapter

    @Inject
    lateinit var bus: RxBus

    override val adapter: ListAdapter<ListItem, RecyclerView.ViewHolder> by lazy { periodAdapter }
    override val screenLayout: Int = R.layout.fragment_statistic_period
    override val classToken: Class<PeriodPm> = PeriodPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider? = null

    override val backgroundColor: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val period = arguments?.getSerializable(EXTRA_PERIOD) as Period
        presentationModel.setPeriod(period)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.glucoseChartView.statisticsChartView.setOnStatisticsDateChangedListener(
            object : OnStatisticsDateChangedListener {
                override fun onUnselectedAll() {
                    bus.click(Clicks.DateInStatisticsClicked(null))
                }

                override fun onDateChanged(date: LocalDate) {
                    bus.click(Clicks.DateInStatisticsClicked(date))
                }
            }
        )
    }

    override fun onBindPresentationModel(pm: PeriodPm) {
        super.onBindPresentationModel(pm)
        bindProgressDialog(pm)
        pm.chartModel.bindTo {
            binding.glucoseChartView.periodDatesTitleView.text = it.datesTitle
            binding.glucoseChartView.statisticsChartView.setChartModel(it.chartModel)
        }
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
