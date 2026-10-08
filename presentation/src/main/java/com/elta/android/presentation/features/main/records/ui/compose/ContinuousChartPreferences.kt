package com.elta.android.presentation.features.main.records.ui.compose

import android.content.Context

internal class ContinuousChartPreferences(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        "glucose_chart_preferences",
        Context.MODE_PRIVATE
    )

    fun readTrendLineStyle(): ContinuousTrendLineStyle =
        runCatching {
            ContinuousTrendLineStyle.valueOf(
                preferences.getString(TREND_LINE_STYLE_KEY, ContinuousTrendLineStyle.SHARP.name)
                    ?: ContinuousTrendLineStyle.SHARP.name
            )
        }.getOrDefault(ContinuousTrendLineStyle.SHARP)

    fun saveTrendLineStyle(style: ContinuousTrendLineStyle) {
        preferences.edit().putString(TREND_LINE_STYLE_KEY, style.name).apply()
    }

    fun shouldShowZoomHint(): Boolean =
        !preferences.getBoolean(ZOOM_HINT_SHOWN_KEY, false)

    fun markZoomHintShown() {
        preferences.edit().putBoolean(ZOOM_HINT_SHOWN_KEY, true).apply()
    }

    private companion object {
        const val TREND_LINE_STYLE_KEY = "continuous_trend_line_style"
        const val ZOOM_HINT_SHOWN_KEY = "continuous_zoom_hint_shown"
    }
}
