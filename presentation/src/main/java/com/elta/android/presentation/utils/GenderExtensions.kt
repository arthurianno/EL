package com.elta.android.presentation.utils

import androidx.annotation.StringRes
import com.elta.android.domain.features.user.model.Gender
import com.elta.android.presentation.R
import com.nullgr.core.resources.ResourceProvider

fun Gender.toString(resource: ResourceProvider): String =
    when (this) {
        Gender.MALE -> resource.getString(R.string.gender_male)
        Gender.FEMALE -> resource.getString(R.string.gender_female)
        Gender.NOT_SPECIFIED -> resource.getString(R.string.gender_not_specified)
    }

@StringRes
fun Gender.toStringRes(): Int =
    when (this) {
        Gender.MALE -> R.string.gender_male
        Gender.FEMALE -> R.string.gender_female
        Gender.NOT_SPECIFIED -> R.string.gender_not_specified
    }
