package com.elta.android.presentation.features.profile.settings.glucoseformat.model

import com.elta.android.domain.features.user.model.GlucoseFormat
import com.elta.android.presentation.core.compose.common.Action

sealed class GlucoseFormatAction : Action {
    data class SelectFormat(val format: GlucoseFormat) : GlucoseFormatAction()
}
