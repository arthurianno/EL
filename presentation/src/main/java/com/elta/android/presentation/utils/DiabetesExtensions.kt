package com.elta.android.presentation.utils

import com.elta.android.domain.features.user.model.Diabetes
import com.elta.android.presentation.R
import com.nullgr.core.resources.ResourceProvider

fun Diabetes.toString(resource: ResourceProvider): String =
    when (this) {
        Diabetes.FIRST -> resource.getString(R.string.diabetes_type_first)
        Diabetes.SECOND -> resource.getString(R.string.diabetes_type_second)
        Diabetes.LADA -> resource.getString(R.string.diabetes_type_lada)
        Diabetes.GESTATIONAL -> resource.getString(R.string.diabetes_type_gestational)
        Diabetes.PREDIABETES -> resource.getString(R.string.diabetes_type_prediabetes)
        Diabetes.OTHER -> resource.getString(R.string.diabetes_type_other)
    }