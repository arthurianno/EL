package com.elta.android.domain.features.diary.events.model.form

import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.diary.home.model.CalculatorFlow
import com.elta.android.domain.features.diary.medicines.model.InsulinMedicament
import com.elta.android.domain.features.diary.home.model.DoubleRange
import com.elta.android.domain.features.diary.medicines.model.Medicament
import org.threeten.bp.ZonedDateTime

private const val BOTTOM_LEVEL_INCLUSIVE = 0.1
private const val TOP_LEVEL_INCLUSIVE = 99.9
private val VALUE_DIAPASON = DoubleRange(BOTTOM_LEVEL_INCLUSIVE, TOP_LEVEL_INCLUSIVE)
private const val KIND_MAX_LENGTH = 40


class BreadValidator(private val calculatorFlow: CalculatorFlow?) : FormValidator {

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
        val areFlowFieldsValid =
            if (calculatorFlow == CalculatorFlow.BREAD_UNITS) validateValue(value)
            else validateDishes(dishes) && validateKind(kind)

        return areFlowFieldsValid && date != null && note.noteIsValid()
    }

    private fun validateValue(value: Double?): Boolean =
        value != null && value in VALUE_DIAPASON

    private fun validateDishes(dishes: List<Dish>?): Boolean = dishes?.isNotEmpty() == true

    private fun validateKind(kind: String?): Boolean =
        kind.orEmpty().length <= KIND_MAX_LENGTH && kind.symbolsIsValid()
}
