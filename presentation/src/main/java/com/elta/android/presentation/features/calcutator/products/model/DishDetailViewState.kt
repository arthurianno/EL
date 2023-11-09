package com.elta.android.presentation.features.calcutator.products.model

import androidx.compose.runtime.Immutable

@Immutable
data class DishDetailViewState(
    val dish: DishUiEntity,
    val startDish: DishUiEntity,
    val isShowCountHelpSnack: Boolean,
    val isLoading: Boolean,
    val isError: Boolean,
)
