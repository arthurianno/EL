package com.elta.android.presentation.features.calcutator.model

import com.elta.android.domain.features.user.model.Profile

data class CalculatorState(
    val profile: Profile = Profile()
)
