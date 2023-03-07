package com.elta.android.domain.features.diary.events.model.form

import com.elta.android.domain.features.diary.events.model.Insulin
import com.elta.android.domain.features.diary.home.model.DoubleRange
import org.threeten.bp.ZonedDateTime

object InsulinValidator : FormValidator {

    private const val bottomLevelInclusive = 0.1
    private const val topLevelInclusive = 99.9
    private val valueDiapason = DoubleRange(bottomLevelInclusive, topLevelInclusive)

    override fun isValid(
        value: Double?,
        kind: String?,
        name: String?,
        duration: Long?,
        insulin: Insulin?,
        date: ZonedDateTime?,
        note: String?
    ): Boolean = validateValue(value) && insulin != null && date != null && note.noteIsValid()

    private fun validateValue(value: Double?): Boolean = value != null && value in valueDiapason
}
