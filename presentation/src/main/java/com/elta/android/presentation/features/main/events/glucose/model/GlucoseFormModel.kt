package com.elta.android.presentation.features.main.events.glucose.model

import com.elta.android.domain.features.diary.events.model.MealTag
import com.elta.android.domain.features.diary.tags.model.Tag

data class GlucoseFormModel(
    val tag: Tag? = null,
    val noteValue: String? = null,
    val mealTag: MealTag? = null
)
