package com.elta.android.domain.features.diary.events.model.form

import com.elta.android.domain.features.diary.events.model.Medicament
import com.elta.android.domain.features.diary.home.model.DoubleRange
import org.threeten.bp.ZonedDateTime

object WeightValidator : FormValidator {

    private const val bottomLevelInclusive = 0.1
    private const val topLevelInclusive = 200.9
    private val valueDiapason = DoubleRange(bottomLevelInclusive, topLevelInclusive)

    override fun isValid(
        value: Double?,
        kind: String?,
        name: String?,
        duration: Long?,
        medicament: Medicament?,
        date: ZonedDateTime?,
        note: String?
    ): Boolean = validateValue(value) && date != null && note.noteIsValid()

    private fun validateValue(value: Double?): Boolean = value != null && value in valueDiapason
}
