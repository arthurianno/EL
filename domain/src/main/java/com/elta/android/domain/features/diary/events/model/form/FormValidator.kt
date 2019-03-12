package com.elta.android.domain.features.diary.events.model.form

import com.elta.android.domain.features.diary.events.model.InsulinType
import java.util.Date

interface FormValidator {

    @Suppress("LongParameterList")
    fun isValid(
        value: Double? = null,
        kind: String? = null,
        name: String? = null,
        duration: Long? = null,
        insulin: InsulinType? = null,
        date: Date? = null,
        note: String? = null
    ): Boolean

    fun validateNote(note: String?): Boolean = note?.length ?: 0 <= NOTE_MAX_LENGTH

    companion object {
        const val NOTE_MAX_LENGTH = 120
    }
}