package com.elta.android.presentation.features.profile.settings.dialogs.glucose.model

data class GlucoseLevel(
    val beforeMeal: GlucoseRange,
    val afterMeal: GlucoseRange
) {
    fun isNotEmpty() =
        beforeMeal.minLevel.isNotEmpty() && beforeMeal.maxLevel.isNotEmpty() &&
                afterMeal.minLevel.isNotEmpty() && afterMeal.maxLevel.isNotEmpty()
}
