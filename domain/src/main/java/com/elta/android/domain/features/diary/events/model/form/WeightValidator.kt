package com.elta.android.domain.features.diary.events.model.form

import com.elta.android.domain.features.diary.events.model.InsulinType
import com.elta.android.domain.features.diary.home.model.DoubleRange
import java.util.Date

object WeightValidator : FormValidator {

    const val bottomLevelInclusive = 0.1
    const val topLevelInclusive = 200.9
    private val valueDiapason = DoubleRange(bottomLevelInclusive, topLevelInclusive)

    override fun isValid(
        value: Double?,
        kind: String?,
        name: String?,
        duration: Long?,
        insulin: InsulinType?,
        date: Date?,
        note: String?
    ): Boolean = validateValue(value) && date != null

    private fun validateValue(value: Double?): Boolean = value != null && value in valueDiapason
}