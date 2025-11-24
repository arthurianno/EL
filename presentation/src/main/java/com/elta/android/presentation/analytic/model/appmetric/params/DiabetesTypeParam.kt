package com.elta.android.presentation.analytic.model.appmetric.params

import androidx.annotation.StringDef

@StringDef(
    DiabetesTypeParam.DIABETES_FIRST,
    DiabetesTypeParam.DIABETES_SECOND_PILLS,
    DiabetesTypeParam.DIABETES_SECOND_INSULIN
)
@Retention(AnnotationRetention.RUNTIME)
annotation class DiabetesTypeParam {
    companion object {
        const val DIABETES_FIRST = "diabetes_first"
        const val DIABETES_SECOND_PILLS = "diabetes_second_pills"
        const val DIABETES_SECOND_INSULIN = "diabetes_second_insulin"
    }
}
