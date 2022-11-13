package com.elta.android.presentation.features.calcutator.model

import androidx.compose.runtime.Immutable

@Immutable
data class DishState(
    val dish: DishUi,
    val portion: PortionUi
)
