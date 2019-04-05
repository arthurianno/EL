package com.elta.android.presentation.utils

import android.support.annotation.StringRes
import com.elta.android.domain.features.user.model.Gender
import com.elta.android.presentation.R
import com.nullgr.core.resources.ResourceProvider

fun Gender.toString(resource: ResourceProvider): String =
    when (this) {
        Gender.MALE -> resource.getString(R.string.on_boarding_gender_male)
        Gender.FEMALE -> resource.getString(R.string.on_boarding_gender_female)
    }

@StringRes
fun Gender.toStringRes(): Int =
    when (this) {
        Gender.MALE -> R.string.on_boarding_gender_male
        Gender.FEMALE -> R.string.on_boarding_gender_female
    }