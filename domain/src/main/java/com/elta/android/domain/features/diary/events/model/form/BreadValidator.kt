package com.elta.android.domain.features.diary.events.model.form

import com.elta.android.domain.features.diary.events.model.InsulinType
import com.elta.android.domain.features.diary.home.model.DoubleExclusiveRange
import java.util.Date

object BreadValidator : FormValidator {

    const val bottomLevelInclusive = 0.1
    const val topLevelExclusive = 100.0
    private val valueDiapason = DoubleExclusiveRange(bottomLevelInclusive, topLevelExclusive)
    private const val kindMaxLength = 40

    override fun isValid(
        value: Double?,
        kind: String?,
        name: String?,
        duration: Long?,
        insulin: InsulinType?,
        date: Date?,
        note: String?
    ): Boolean = validateValue(value) && validateKind(kind) && date != null && validateNote(note)

    private fun validateValue(value: Double?): Boolean = value != null && value in valueDiapason
    private fun validateKind(kind: String?): Boolean = kind?.length ?: 0 <= kindMaxLength
}