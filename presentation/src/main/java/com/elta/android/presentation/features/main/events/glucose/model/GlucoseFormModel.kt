package com.elta.android.presentation.features.main.events.glucose.model

import com.elta.android.domain.features.diary.tags.model.Tag

data class GlucoseFormModel(
    var tag: Tag? = null,
    var noteValue: String? = null
) {
    val note: String?
        get() = if (noteValue.isNullOrEmpty()) null else noteValue
}
