package com.elta.android.presentation.features.consultant.model

import androidx.compose.runtime.Immutable

@Immutable
data class RatingUiEntity(
    val isRatingMessageShowing: Boolean,
    val starsCount: Int?
)
