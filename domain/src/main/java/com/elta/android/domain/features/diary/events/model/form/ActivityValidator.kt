package com.elta.android.domain.features.diary.events.model.form

import com.elta.android.domain.features.diary.events.model.Insulin
import org.threeten.bp.ZonedDateTime

object ActivityValidator : FormValidator {

    override fun isValid(
        value: Double?,
        kind: String?,
        name: String?,
        duration: Long?,
        insulin: Insulin?,
        date: ZonedDateTime?,
        note: String?
    ): Boolean = isValidDuration(duration) && date != null && note.noteIsValid()

    private fun isValidDuration(duration: Long?): Boolean = duration != null && duration > 0
}
