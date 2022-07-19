package com.elta.android.domain.features.diary.events.model.form

import com.elta.android.domain.features.diary.events.model.InsulinType
import org.threeten.bp.ZonedDateTime

interface FormValidator {

    @Suppress("LongParameterList")
    fun isValid(
        value: Double? = null,
        kind: String? = null,
        name: String? = null,
        duration: Long? = null,
        insulin: InsulinType? = null,
        date: ZonedDateTime? = null,
        note: String? = null
    ): Boolean

    fun validateNote(note: String?): Boolean =
        (note?.length ?: 0) <= NOTE_MAX_LENGTH && validateSpecSymbols(note)

    fun validateSpecSymbols(string: String?) = if (string == null) {
        true
    } else {
        !(
            string.any { !it.isLetterOrDigit() } &&
                string.all { !it.isLetterOrDigit() }
            )
    }

    companion object {
        const val NOTE_MAX_LENGTH = 120
    }
}
