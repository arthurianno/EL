package com.elta.android.domain.features.diary.home.model

import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.user.model.GlucoseFormat

data class GlucoseSharingInfo(
    val event: Event,
    val glucoseFormat: GlucoseFormat,
    val glucoseLevelSettings: GlucoseLevelSettings
)
