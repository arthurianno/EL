package com.elta.android.domain.features.diary.events.model

import android.os.Parcelable
import com.elta.android.domain.features.diary.home.model.CalculatorFlow
import kotlinx.parcelize.Parcelize

@Parcelize
sealed interface EventType : Parcelable {
    @Parcelize
    data class Bread(val calculatorFlow: CalculatorFlow) : EventType
    @Parcelize
    object Insulin : EventType
    @Parcelize
    object Medicaments : EventType
    @Parcelize
    object Activity : EventType
    @Parcelize
    object Weight : EventType
    @Parcelize
    object Glucose : EventType
    @Parcelize
    object Glycatedhemoglobin: EventType
}
