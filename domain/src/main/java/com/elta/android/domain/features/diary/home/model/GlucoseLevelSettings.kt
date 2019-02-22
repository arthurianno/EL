package com.elta.android.domain.features.diary.home.model

data class GlucoseLevelSettings(
    val high: DoubleExclusiveRange = DoubleExclusiveRange(HIGH_START, Double.MAX_VALUE),
    val normal: DoubleExclusiveRange = DoubleExclusiveRange(NORMAL_START, NORMAL_END),
    val low: DoubleExclusiveRange = DoubleExclusiveRange(LOW_START, LOW_END)
) {
    companion object {
        const val HIGH_START = 10.0
        const val NORMAL_END = 10.1
        const val NORMAL_START = 4.0
        const val LOW_END = 4.0
        const val LOW_START = 0.0
    }
}