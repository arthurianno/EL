package com.elta.android.presentation.utils

import com.elta.android.domain.features.user.model.Profile

fun Profile.createFullName(placeholder: String): String {
    return when {
        firstName.isNullOrEmpty() && secondName.isNullOrEmpty() -> placeholder
        firstName.isNullOrEmpty() -> secondName.orEmpty()
        secondName.isNullOrEmpty() -> firstName.orEmpty()
        else -> "$firstName $secondName"
    }
}
