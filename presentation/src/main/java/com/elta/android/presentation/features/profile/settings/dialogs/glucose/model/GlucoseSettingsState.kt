package com.elta.android.presentation.features.profile.settings.dialogs.glucose.model

import com.elta.android.domain.features.user.model.Profile
import com.google.errorprone.annotations.Immutable

@Immutable
data class GlucoseSettingsState(
    val currentGlucoseLevel: GlucoseLevel,
    val startGlucoseLevel: GlucoseLevel,
    val errorTypeBeforeMeal: GlucoseRangeError,
    val errorTypeAfterMeal: GlucoseRangeError,
    val profile: Profile?,
    val isLoading: Boolean
)
