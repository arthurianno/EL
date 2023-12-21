package com.elta.android.domain.features.diary.events.model.form

import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.diary.medicines.model.InsulinMedicament
import com.elta.android.domain.features.diary.medicines.model.Medicament
import org.threeten.bp.ZonedDateTime

object MedicamentsValidator : FormValidator {

    private const val nameOtherMaxLength = 50
    private val diapason = 1..nameOtherMaxLength

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
    ): Boolean {

        if (date == null) return false

        return medicament?.let {
            val nameIsValid = if (medicament.isOther) name.nameIsValid() else true
            nameIsValid && tabletsNumber.isValid()
        } ?: if (flowIsEdit == true) name.nameIsValid() else false
    }

    private fun String?.nameIsValid() = !this.isNullOrBlank() && length in diapason

    private fun Double?.isValid(): Boolean {
        return this != 0.0 && this != null
    }
}
