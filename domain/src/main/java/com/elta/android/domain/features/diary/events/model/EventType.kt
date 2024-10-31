package com.elta.android.domain.features.diary.events.model

import android.os.Parcelable
import com.elta.android.domain.features.diary.home.model.CalculatorFlow
import kotlinx.parcelize.Parcelize

@Parcelize
sealed interface EventType : Parcelable {
    @Parcelize
    data class Bread(val calculatorFlow: CalculatorFlow) : EventType
    @Parcelize
    data object Insulin : EventType
    @Parcelize
    data object Medicaments : EventType
    @Parcelize
    data object Activity : EventType
    @Parcelize
    data object Weight : EventType
    @Parcelize
    data class Glucose(val inputType : GlucoseInputType) : EventType
    @Parcelize
    data object Glycatedhemoglobin: EventType
}
