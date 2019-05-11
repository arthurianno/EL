package com.elta.android.presentation.widgets.charts.statistics

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.elta.android.presentation.R
import com.elta.android.presentation.utils.NumberFormatter
import com.elta.android.presentation.widgets.charts.statistics.listeners.OnStatisticsDateChangedListener
import com.elta.android.presentation.widgets.charts.statistics.listeners.StatisticsDateChangedObserver
import com.elta.android.presentation.widgets.charts.statistics.models.StatisticsChartDataModel
import io.reactivex.Observable
import kotlinx.android.synthetic.main.layout_statistics_chart_compound_view.view.*
import java.util.Date

@Suppress("MagicNumber")
class StatisticsChartCompoundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_statistics_chart_compound_view, this, true)
    }

    fun setOnStatisticsDateChangedListener(listener: OnStatisticsDateChangedListener) {
        chartView.setOnStatisticsDateChangedListener(listener)
    }

    fun dateChanged(): Observable<Date> = StatisticsDateChangedObserver(chartView)
        .flatMap {
            when (it.date != null) {
                true -> Observable.just(it.date)
                else -> Observable.empty()
            }
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
        private const val SCROLL_DELAY = 100L
    }
}
