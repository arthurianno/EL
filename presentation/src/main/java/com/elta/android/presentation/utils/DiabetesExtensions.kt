package com.elta.android.presentation.utils

import androidx.annotation.StringRes
import com.elta.android.domain.features.user.model.Diabetes
import com.elta.android.presentation.R
import com.nullgr.core.resources.ResourceProvider

fun Diabetes.toString(resource: ResourceProvider): String =
    when (this) {
        Diabetes.FIRST -> resource.getString(R.string.diabetes_type_first)
        Diabetes.SECOND -> resource.getString(R.string.diabetes_type_second)
        Diabetes.SECOND_TABLETS -> resource.getString(R.string.diabetes_type_second_tablets)
    }

@StringRes
fun Diabetes.toStringRes(): Int =
    when (this) {
        Diabetes.FIRST -> R.string.diabetes_type_first
        Diabetes.SECOND -> R.string.diabetes_type_second
        Diabetes.SECOND_TABLETS -> R.string.diabetes_type_second_tablets
    }
