package com.elta.android.presentation.widgets.charts.daily

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.Menu
import android.widget.FrameLayout
import android.widget.PopupMenu
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
        binding.dailyGlucoseChartView.trendLineStyle = storedTrendLineStyle()
        binding.dailyGlucoseTrendSettingsView.setOnClickListener { showTrendLineStyleMenu() }
    }

    fun setChartDataModel(chartDataModel: ChartDataModel) = with(binding) {
        dailyGlucoseChartView.chartDataModel = chartDataModel
        //dailyGlucoseChartView.chartDataModel = binding.dailyGlucoseChartView.createMockChartDataModel()
        scrollToLastEvent()
    }

    private fun scrollToLastEvent() = with(binding) {
        postDelayed({
            val scrollX = dailyGlucoseChartView.getScrollPosition()
            dailyGlucoseHorizontalView.scrollTo(scrollX.toInt(), 0)
        }, SCROLL_DELAY)
    }

    private fun showTrendLineStyleMenu() {
        PopupMenu(context, binding.dailyGlucoseTrendSettingsView).apply {
            menu.setGroupCheckable(TREND_LINE_MENU_GROUP, true, true)
            menu.add(
                TREND_LINE_MENU_GROUP,
                GlucoseTrendLineStyle.SHARP.ordinal,
                Menu.NONE,
                context.getString(R.string.glucose_trend_line_sharp)
            ).isCheckable = true
            menu.add(
                TREND_LINE_MENU_GROUP,
                GlucoseTrendLineStyle.SMOOTH.ordinal,
                Menu.NONE,
                context.getString(R.string.glucose_trend_line_smooth)
            ).isCheckable = true

            menu.findItem(storedTrendLineStyle().ordinal).isChecked = true
            setOnMenuItemClickListener { item ->
                val style = GlucoseTrendLineStyle.entries[item.itemId]
                preferences.edit().putString(TREND_LINE_STYLE_KEY, style.name).apply()
                binding.dailyGlucoseChartView.trendLineStyle = style
                true
            }
            show()
        }
    }

    private fun storedTrendLineStyle(): GlucoseTrendLineStyle = runCatching {
        GlucoseTrendLineStyle.valueOf(
            preferences.getString(TREND_LINE_STYLE_KEY, GlucoseTrendLineStyle.SHARP.name)
                ?: GlucoseTrendLineStyle.SHARP.name
        )
    }.getOrDefault(GlucoseTrendLineStyle.SHARP)

    private val preferences by lazy {
        context.applicationContext.getSharedPreferences(TREND_LINE_PREFERENCES, Context.MODE_PRIVATE)
    }

    companion object {
        private const val SCROLL_DELAY = 200L
        private const val TREND_LINE_MENU_GROUP = 1
        private const val TREND_LINE_PREFERENCES = "glucose_daily_chart_preferences"
        private const val TREND_LINE_STYLE_KEY = "trend_line_style"
    }
}
