package com.elta.android.presentation.widgets.charts.daily.models

data class ChartRangesModel(
    val start: Double,
    val end: Double,
    val normalMax: Double,
    val lowMax: Double?,
    val highMax: Double?
) {
    val needDrawLow: Boolean
        get() = lowMax != null

    val needDrawHigh: Boolean
        get() = highMax != null

    /**
     * Множитель для увеличения высоты графика при больших диапазонах высоких значений.
     * Обеспечивает лучшую визуальную различимость точек при экстремальных значениях глюкозы.
     */
    val heightMultiplier: Float
        get() {
            val highRange = highMax?.let { it - normalMax } ?: 0.0
            return when {
                highRange > 20 -> 1.8f  // Очень большой диапазон (>20 ммоль/л) - увеличить в 1.8 раза
                highRange > 15 -> 1.5f  // Большой диапазон (>15 ммоль/л) - увеличить в 1.5 раза
                highRange > 10 -> 1.3f  // Средний диапазон (>10 ммоль/л) - увеличить в 1.3 раза
                else -> 1.0f            // Нормальный диапазон - стандартная высота
            }
        }
}

