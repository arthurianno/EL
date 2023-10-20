package com.elta.android.domain.features.diary.home.model

import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.domain.features.user.model.GlucoseFormat

data class GlucoseSharingInfo(
    val event: EventV2,
    val glucoseFormat: GlucoseFormat,
    val glucoseLevelSettings: GlucoseLevelSettings
)
