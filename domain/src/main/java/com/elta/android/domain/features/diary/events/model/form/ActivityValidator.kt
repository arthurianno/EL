package com.elta.android.domain.features.diary.events.model.form

import com.elta.android.domain.features.diary.events.model.Medicament
import org.threeten.bp.ZonedDateTime

object ActivityValidator : FormValidator {

    override fun isValid(
        value: Double?,
        kind: String?,
        name: String?,
        duration: Long?,
        medicament: Medicament?,
        date: ZonedDateTime?,
        note: String?
    ): Boolean = isValidDuration(duration) && date != null && note.noteIsValid()

    fun isValidDuration(duration: Long?): Boolean = duration != null && duration > 0
}
