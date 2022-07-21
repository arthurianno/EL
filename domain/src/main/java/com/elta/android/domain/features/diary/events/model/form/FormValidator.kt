package com.elta.android.domain.features.diary.events.model.form

import com.elta.android.domain.features.diary.events.model.Insulin
import org.threeten.bp.ZonedDateTime

interface FormValidator {

    @Suppress("LongParameterList")
    fun isValid(
        value: Double? = null,
        kind: String? = null,
        name: String? = null,
        duration: Long? = null,
        insulin: Insulin? = null,
        date: ZonedDateTime? = null,
        note: String? = null
    ): Boolean

    fun isValidNote(note: String?): Boolean =
        (note?.length ?: 0) <= NOTE_MAX_LENGTH && isContainsOnlySpecSymbols(note?.trim())

    fun isContainsOnlySpecSymbols(string: String?) =
        string?.all { !it.isLetterOrDigit() }?.not() ?: true

    companion object {
        const val NOTE_MAX_LENGTH = 120
    }
}
