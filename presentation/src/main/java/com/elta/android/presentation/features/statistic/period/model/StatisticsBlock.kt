package com.elta.android.presentation.features.statistic.period.model

enum class StatisticsBlock(
    val title: String,
    val subtitle: String
) {
    PERIOD("Показатели за период", "Количество измерений"),
    DAILY("Суточные колебания", "Недельный график"),
    KEY_METRICS("Ключевые метрики", "CV, SD, GMI и др"),
    COMPARISON("Сравнение", "С предыдущим периодом"),
    ACTIVITY("Активность", "Отображение шагов, тренировок"),
    FOOD("Питание", "Подсчёт БЖУ и ХЕ.")
}

internal val DEFAULT_VISIBLE_STATISTICS_BLOCKS = listOf(
    StatisticsBlock.PERIOD,
    StatisticsBlock.DAILY,
    StatisticsBlock.KEY_METRICS,
    StatisticsBlock.COMPARISON
)
