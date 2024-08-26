package com.elta.android.domain.features.diary.events.model.form

import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.diary.medicines.model.InsulinMedicament
import com.elta.android.domain.features.diary.medicines.model.Medicament
import org.threeten.bp.ZonedDateTime
import timber.log.Timber

private const val NOTE_MAX_LENGTH = 120

interface FormValidator {

    @Suppress("LongParameterList")
    fun isValid(
        value: Double? = null,
        kind: String? = null,
        name: String? = null,
        duration: Long? = null,
        insulinMedicament: InsulinMedicament? = null,
        medicament: Medicament? = null,
        tabletsNumber: Double? = null,
        dishes: List<Dish>? = null,
        flowIsEdit: Boolean? = null,
        date: ZonedDateTime? = null,
        note: String? = null,
    ): Boolean
}

internal fun String?.noteIsValid(): Boolean {
    return (this.orEmpty().length <= NOTE_MAX_LENGTH) && this.symbolsIsValid()
}

internal fun String?.symbolsIsValid(): Boolean {
    return this.orEmpty().isEmpty() || this.orEmpty().any { it.isLetterOrDigit() } ||
            this.orEmpty().isBlank()
}
