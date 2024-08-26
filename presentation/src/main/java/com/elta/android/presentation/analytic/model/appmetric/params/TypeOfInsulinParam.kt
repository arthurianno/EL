package com.elta.android.presentation.analytic.model.appmetric.params

import androidx.annotation.StringDef

@StringDef(
    TypeOfInsulinParam.SHORT,
    TypeOfInsulinParam.LONG,
    TypeOfInsulinParam.MIXED
)
@Retention(AnnotationRetention.RUNTIME)
annotation class TypeOfInsulinParam {
    companion object {
        const val SHORT = "short_acting"
        const val LONG = "long_acting"
        const val MIXED = "mixed"
    }
}
