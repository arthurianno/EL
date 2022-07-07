package com.elta.android.presentation.widgets.charts.statistics

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.elta.android.presentation.R
import com.elta.android.presentation.databinding.LayoutStatisticsChartCompoundViewBinding
import com.elta.android.presentation.utils.NumberFormatter
import com.elta.android.presentation.widgets.charts.statistics.listeners.OnStatisticsDateChangedListener
import com.elta.android.presentation.widgets.charts.statistics.models.StatisticsChartDataModel

@Suppress("MagicNumber")
class StatisticsChartCompoundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: LayoutStatisticsChartCompoundViewBinding by lazy {
        LayoutStatisticsChartCompoundViewBinding.bind(this)
    }

    private var isCreate: Boolean = true

    init {
        LayoutInflater.from(context)
            .inflate(R.layout.layout_statistics_chart_compound_view, this, true)
    }

    fun setOnStatisticsDateChangedListener(listener: OnStatisticsDateChangedListener) {
        binding.chartView.setOnStatisticsDateChangedListener(listener)
    }

    fun setChartModel(model: StatisticsChartDataModel) {
        bindValues(model.values)
        binding.chartView.chartDataModel = model
        binding.chartView.requestLayout()
        if (isCreate) {
            scrollToEnd()
            isCreate = false
        }
    }

    private fun scrollToEnd() = with(binding) {
        postDelayed({
            val scrollX = chartView.getScrollPosition()
            statisticsScrollView.scrollTo(scrollX.toInt(), 0)
        }, SCROLL_DELAY)
    }

    private fun bindValues(values: List<Double>) = with(binding) {
        firstValueView.text = NumberFormatter.format(values.component1())
        secondValueView.text = NumberFormatter.format(values.component2())
        thirdValueView.text = NumberFormatter.format(values.component3())
        fourthValueView.text = NumberFormatter.format(values.component4())
        fifthValueView.text = NumberFormatter.format(values.component5())
        sixValueView.text = NumberFormatter.format(values.last())
    }

    companion object {
        private const val SCROLL_DELAY = 100L
    }
}
