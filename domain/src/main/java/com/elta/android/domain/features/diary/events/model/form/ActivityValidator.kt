package com.elta.android.domain.features.diary.events.model.form

import com.elta.android.domain.features.diary.events.model.InsulinType
import org.threeten.bp.ZonedDateTime

object ActivityValidator : FormValidator {

    override fun isValid(
        value: Double?,
        kind: String?,
        name: String?,
        duration: Long?,
        insulin: InsulinType?,
        date: ZonedDateTime?,
        note: String?
    ): Boolean = validateDuration(duration) && date != null && validateNote(note)

    private fun validateDuration(duration: Long?): Boolean = duration != null && duration > 0
}
