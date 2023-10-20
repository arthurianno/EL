package com.elta.android.domain.features.diary.events.model.form

import com.elta.android.domain.features.diary.events.model.Medicament
import org.threeten.bp.ZonedDateTime

private const val NOTE_MAX_LENGTH = 120

interface FormValidator {

    @Suppress("LongParameterList")
    fun isValid(
        value: Double? = null,
        kind: String? = null,
        name: String? = null,
        duration: Long? = null,
        medicament: Medicament? = null,
        date: ZonedDateTime? = null,
        note: String? = null
    ): Boolean
}

internal fun String?.noteIsValid(): Boolean =
    (this.orEmpty().length <= NOTE_MAX_LENGTH) && this.symbolsIsValid()

internal fun String?.symbolsIsValid(): Boolean =
    this.orEmpty().isEmpty() || this.orEmpty().any { it.isLetterOrDigit() }
