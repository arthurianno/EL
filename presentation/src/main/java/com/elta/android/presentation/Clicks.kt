package com.elta.android.presentation

import com.elta.android.domain.features.user.model.Gender
import com.elta.android.presentation.core.bus.Click

sealed class Clicks : Click {

    data class GenderSelected(val newGender: Gender?) : Clicks()
}