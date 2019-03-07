package com.elta.android.domain.features.diary.events.model.form

import com.elta.android.domain.features.diary.events.model.ActivityType
import com.elta.android.domain.features.diary.events.model.InsulinType
import com.elta.android.domain.features.diary.tags.model.Tag
import java.util.Date

object ActivityValidator : FormValidator {

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
    ): Boolean = validateDuration(duration) && date != null && validateNote(note)


    private fun validateDuration(duration: Long?): Boolean = duration != null && duration > 0
}