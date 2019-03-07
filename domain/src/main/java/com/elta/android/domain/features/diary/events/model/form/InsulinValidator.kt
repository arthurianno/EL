package com.elta.android.domain.features.diary.events.model.form

import com.elta.android.domain.features.diary.events.model.ActivityType
import com.elta.android.domain.features.diary.events.model.InsulinType
import com.elta.android.domain.features.diary.home.model.DoubleExclusiveRange
import com.elta.android.domain.features.diary.tags.model.Tag
import java.util.Date

object InsulinValidator : FormValidator {

    private val valueDiapason = DoubleExclusiveRange(0.1, 100.0)

    override fun isValid(
        value: Double?,
        kind: String?,
        name: String?,
        duration: Long?,
        date: Date?,
        tag: Tag?,
        insulin: InsulinType?,
        activity: ActivityType?,
        note: String?
    ): Boolean = validateValue(value) && insulin != null && date != null && validateNote(note)

    private fun validateValue(value: Double?): Boolean = value != null && value in valueDiapason
}