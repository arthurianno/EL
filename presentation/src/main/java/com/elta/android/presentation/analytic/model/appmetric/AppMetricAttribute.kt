package com.elta.android.presentation.analytic.model.appmetric

import com.elta.android.presentation.analytic.model.appmetric.params.DiabetesTypeParam
import io.appmetrica.analytics.profile.GenderAttribute

sealed class AppMetricAttribute {

    data class Email(val emailAddress: String) : AppMetricAttribute()
    data class DiabetesType(@DiabetesTypeParam val type: String) : AppMetricAttribute()
    data class Gender(val gender: GenderAttribute.Gender) : AppMetricAttribute()
    data class Age(val years: Int) : AppMetricAttribute()
    data class Emias(val registered: String) : AppMetricAttribute()
}
