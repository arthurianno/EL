package com.elta.android.domain.features.diary.events.model.form

import com.elta.android.domain.features.diary.events.model.ActivityType
import com.elta.android.domain.features.diary.events.model.InsulinType
import com.elta.android.domain.features.diary.tags.model.Tag
import java.util.Date

interface FormValidator {

    fun isValid(
        value: Double? = null,
        kind: String? = null,
        name: String? = null,
        duration: Long? = null,
        date: Date? = null,
        tag: Tag? = null,
        insulin: InsulinType? = null,
        activity: ActivityType? = null,
        note: String? = null
    ): Boolean

    fun validateNote(note: String?): Boolean = note?.length ?: 0 <= noteMaxLength

    companion object {
        const val noteMaxLength = 120
    }
}