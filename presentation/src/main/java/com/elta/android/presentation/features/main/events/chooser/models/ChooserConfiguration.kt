package com.elta.android.presentation.features.main.events.chooser.models

import android.os.Parcelable
import com.elta.android.domain.features.events.model.UserEvent
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChooserConfiguration(
    val chooserType: ChooserType,
    val eventType: UserEvent // TODO should be replaced with actual class
) : Parcelable