package com.elta.android.domain.features.diary.events.model.form

import com.elta.android.domain.features.diary.events.model.Insulin
import org.threeten.bp.ZonedDateTime

object MedicamentsValidator : FormValidator {

    private const val nameMaxLength = 120
    private val diapason = 1..nameMaxLength

    override fun isValid(
        value: Double?,
        kind: String?,
        name: String?,
        duration: Long?,
        insulin: Insulin?,
        date: ZonedDateTime?,
        note: String?
    ): Boolean = validateName(name) && date != null && name.symbolsIsValid()

    private fun validateName(name: String?): Boolean =
        if (name == null) false else name.length in diapason
}
