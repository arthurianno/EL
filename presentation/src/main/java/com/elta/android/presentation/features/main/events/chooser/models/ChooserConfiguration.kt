package com.elta.android.presentation.features.main.events.chooser.models

import android.os.Parcelable
import com.elta.android.domain.features.diary.events.model.EventType
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChooserConfiguration(
    val chooserType: ChooserType,
    val eventType: EventType
) : Parcelable