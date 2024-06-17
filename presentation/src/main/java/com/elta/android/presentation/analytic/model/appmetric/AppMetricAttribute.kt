package com.elta.android.presentation.analytic.model.appmetric

import com.elta.android.presentation.analytic.model.appmetric.params.DiabetesTypeParam

sealed class AppMetricAttribute(
    val key: String,
    val value: String
) {
    data class Email(val emailAddress: String) :
        AppMetricAttribute(key = "e-mail", value = emailAddress)

    data class DiabetesType(@DiabetesTypeParam val type: String) :
        AppMetricAttribute(key = "type_diabetes", value = type)
}
