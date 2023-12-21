package com.elta.android.domain.features.diary.home.model

import android.os.Parcelable
import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.domain.features.user.model.Diabetes
import kotlinx.parcelize.Parcelize

@Parcelize
enum class CalculatorFlow : Parcelable {
    BREAD_UNITS, PRODUCT_ONLY;

    companion object {
        fun Diabetes?.toCalculatorFlow(event: List<EventV2>? = null, dishes: List<Dish>? = null): CalculatorFlow {

            val breadUnits = if (dishes != null) {
                dishes.sumOf { it.breadUnits ?: 0.0 } != 0.0
            } else {
                event?.sumOf { it.value ?: 0.0 } != 0.0
            }

            return when {
                this in listOf(Diabetes.FIRST, Diabetes.SECOND, null) && breadUnits -> BREAD_UNITS
                else -> PRODUCT_ONLY
            }
        }
    }
}
