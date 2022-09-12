package com.elta.android.presentation.widgets.charts.daily

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.elta.android.presentation.R
import com.elta.android.presentation.databinding.LayoutGlucoseDailyViewBinding
import com.elta.android.presentation.widgets.charts.daily.models.ChartDataModel

class GlucoseDailyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: LayoutGlucoseDailyViewBinding by lazy {
        LayoutGlucoseDailyViewBinding.bind(this)
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_glucose_daily_view, this, true)
        binding.dailyGlucoseChartView.attachRangesOverlay(binding.dailyGlucoseRangesOverlayView)
    }

    fun setChartDataModel(chartDataModel: ChartDataModel) = with(binding) {
        dailyGlucoseChartView.chartDataModel = chartDataModel
        scrollToLastEvent()
    }

    private fun scrollToLastEvent() = with(binding) {
        postDelayed({
            val scrollX = dailyGlucoseChartView.getScrollPosition()
            dailyGlucoseHorizontalView.scrollTo(scrollX.toInt(), 0)
        }, SCROLL_DELAY)
    }

    companion object {
        private const val SCROLL_DELAY = 200L
    }
}
