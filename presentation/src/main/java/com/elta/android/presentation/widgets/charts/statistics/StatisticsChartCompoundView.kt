package com.elta.android.presentation.widgets.charts.statistics

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.elta.android.presentation.R
import com.elta.android.presentation.utils.NumberFormatter
import com.elta.android.presentation.widgets.charts.statistics.models.StatisticsChartDataModel
import kotlinx.android.synthetic.main.layout_glucose_daily_view.view.*
import kotlinx.android.synthetic.main.layout_statistics_chart_compound_view.view.*

class StatisticsChartCompoundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_statistics_chart_compound_view, this, true)
    }

    fun setChartModel(model: StatisticsChartDataModel) {
        bindValues(model.values)
        chartView.chartDataModel = model
        scrollToEnd()
    }

    private fun scrollToEnd() {
        postDelayed({
            val scrollX = chartView.getScrollPosition()
            statisticsScrollView.scrollTo(scrollX.toInt(), 0)
        }, SCROLL_DELAY)
    }

    private fun bindValues(values: List<Double>) {
        firstValueView.text = NumberFormatter.format(values[0])
        secondValueView.text = NumberFormatter.format(values[1])
        thirdValueView.text = NumberFormatter.format(values[2])
        fourthValueView.text = NumberFormatter.format(values[3])
        fifthValueView.text = NumberFormatter.format(values[4])
        sixValueView.text = NumberFormatter.format(values[5])
    }

    companion object {
        private const val SCROLL_DELAY = 200L
    }
}
