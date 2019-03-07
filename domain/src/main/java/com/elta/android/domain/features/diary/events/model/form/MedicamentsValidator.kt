package com.elta.android.domain.features.diary.events.model.form

import com.elta.android.domain.features.diary.events.model.ActivityType
import com.elta.android.domain.features.diary.events.model.InsulinType
import com.elta.android.domain.features.diary.tags.model.Tag
import java.util.Date

object MedicamentsValidator : FormValidator {

    private const val nameMaxLength = 120

    override fun isValid(
        value: Double?,
        kind: String?,
        name: String?,
        duration: Long?,
        date: Date?,
        tag: Tag?,
        insulin: InsulinType?,
        activity: ActivityType?,
        note: String?
    ): Boolean = validateName(name) && date != null

    private fun validateName(name: String?): Boolean = if (name == null) false else name.length <= nameMaxLength
}