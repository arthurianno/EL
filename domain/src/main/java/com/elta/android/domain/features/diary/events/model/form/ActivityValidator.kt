package com.elta.android.domain.features.diary.events.model.form

import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.diary.medicines.model.InsulinMedicament
import com.elta.android.domain.features.diary.medicines.model.Medicament
import org.threeten.bp.ZonedDateTime

object ActivityValidator : FormValidator {


    override fun isValid(
        value: Double?,
        kind: String?,
        name: String?,
        duration: Long?,
        insulinMedicament: InsulinMedicament?,
        medicament: Medicament?,
        tabletsNumber: Double?,
        dishes: List<Dish>?,
        flowIsEdit: Boolean?,
        date: ZonedDateTime?,
        note: String?
    ): Boolean = isValidDuration(duration) && date != null && note.noteIsValid()

    fun isValidDuration(duration: Long?): Boolean = duration != null && duration > 0
}
