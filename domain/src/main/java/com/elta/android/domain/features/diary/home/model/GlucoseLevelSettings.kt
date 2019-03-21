package com.elta.android.domain.features.diary.home.model

data class GlucoseLevelSettings(
    val high: DoubleRange = DoubleRange(HIGH_START, HIGH_END),
    val normal: DoubleRange = DoubleRange(NORMAL_START, NORMAL_END),
    val low: DoubleRange = DoubleRange(LOW_START, LOW_END)
) {

    companion object {
        const val STEP = 0.1
        const val NORMAL_END = 10.0
        const val NORMAL_START = 3.9

        const val HIGH_END = 100.0
        const val HIGH_START = NORMAL_END + STEP

        const val LOW_END = NORMAL_START - STEP
        const val LOW_START = 0.0

        fun fromNormalValues(normalStart: Double, normalEnd: Double): GlucoseLevelSettings =
            GlucoseLevelSettings(
                high = DoubleRange(normalStart - STEP, HIGH_END),
                normal = DoubleRange(normalStart, normalEnd),
                low = DoubleRange(LOW_START, normalStart - STEP)
            )
    }
}