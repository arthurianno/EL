package com.elta.android.domain.features.diary.events.model.form

import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.diary.medicines.model.InsulinMedicament
import com.elta.android.domain.features.diary.home.model.DoubleRange
import com.elta.android.domain.features.diary.medicines.model.Medicament
import com.elta.android.domain.features.user.model.GlucoseFormat
import org.threeten.bp.ZonedDateTime

data class ManualGlucoseValidator(val glucoseFormat: GlucoseFormat?) : FormValidator {


    private val valueDiapason = when(glucoseFormat){
        GlucoseFormat.PLASMA -> DoubleRange(BOTTOM_LEVEL_PLASMA, TOP_LEVEL_PLASMA)
        else -> DoubleRange(BOTTOM_LEVEL_CAPILLARY, TOP_LEVEL_CAPILLARY)
    }

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
        note: String?,
    ): Boolean = validateValue(value) && date != null && note.noteIsValid()

    private fun validateValue(value: Double?): Boolean = value != null && value in valueDiapason
}

private const val BOTTOM_LEVEL_CAPILLARY = 1.4
private const val TOP_LEVEL_CAPILLARY = 28.0
private const val BOTTOM_LEVEL_PLASMA = 1.6
private const val TOP_LEVEL_PLASMA = 31.4
