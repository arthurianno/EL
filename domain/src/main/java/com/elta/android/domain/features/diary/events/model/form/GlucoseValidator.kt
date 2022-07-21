package com.elta.android.domain.features.diary.events.model.form

import com.elta.android.domain.features.diary.events.model.Insulin
import com.elta.android.domain.features.diary.home.model.DoubleRange
import org.threeten.bp.ZonedDateTime

object GlucoseValidator : FormValidator {

    const val bottomLevelInclusive = 0.1
    const val topLevelInclusive = 65.0
    private val valueDiapason = DoubleRange(bottomLevelInclusive, topLevelInclusive)

    override fun isValid(
        value: Double?,
        kind: String?,
        name: String?,
        duration: Long?,
        insulin: Insulin?,
        date: ZonedDateTime?,
        note: String?
    ): Boolean = validateValue(value) && date != null && isValidNote(note)

    private fun validateValue(value: Double?): Boolean = value != null && value in valueDiapason
}
