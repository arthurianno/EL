package com.elta.android.domain.features.diary.events.model

import com.elta.android.domain.features.diary.events.model.form.ActivityValidator
import com.elta.android.domain.features.diary.events.model.form.BreadValidator
import com.elta.android.domain.features.diary.events.model.form.FormValidator
import com.elta.android.domain.features.diary.events.model.form.GlucoseValidator
import com.elta.android.domain.features.diary.events.model.form.InsulinValidator
import com.elta.android.domain.features.diary.events.model.form.MedicamentsValidator
import com.elta.android.domain.features.diary.events.model.form.WeightValidator

fun EventType.getValidator(): FormValidator =
    when (this) {
        EventType.BREAD -> BreadValidator
        EventType.INSULIN -> InsulinValidator
        EventType.MEDICAMENTS -> MedicamentsValidator
        EventType.ACTIVITY -> ActivityValidator
        EventType.WEIGHT -> WeightValidator
        EventType.GLUCOSE -> GlucoseValidator
    }