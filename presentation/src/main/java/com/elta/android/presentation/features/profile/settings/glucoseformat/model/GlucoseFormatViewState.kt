package com.elta.android.presentation.features.profile.settings.glucoseformat.model

import androidx.compose.runtime.Immutable
import com.elta.android.domain.features.user.model.Profile

@Immutable
data class GlucoseFormatViewState(
    val profile: Profile
)
