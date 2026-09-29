package com.elta.android.presentation.features.profile.settings.glucoseformat.model

import androidx.compose.runtime.Immutable
import com.elta.android.domain.features.user.model.GlucoseFormat
import com.elta.android.domain.features.user.model.Profile

@Immutable
data class GlucoseFormatViewState(
    val profile: Profile,
    val initGlucoseFormat: GlucoseFormat,
    val isFromDmc: Boolean = false,
    val isOnBoarding: Boolean = false,
    val isVariantA: Boolean = false,
    val selectionMade: Boolean = false,
    val isProfileLoaded: Boolean = false,
    val isSaving: Boolean = false
)
