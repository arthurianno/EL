package com.elta.android.domain.features.diary.events.model.form

import com.elta.android.domain.features.diary.events.model.Insulin
import org.threeten.bp.ZonedDateTime

private const val NOTE_MAX_LENGTH = 120

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
}

internal fun String?.noteIsValid(): Boolean =
    orEmpty().length <= NOTE_MAX_LENGTH && orEmpty().trim().symbolsIsValid()

internal fun String?.symbolsIsValid() = orEmpty().all { it.isLetterOrDigit() }
