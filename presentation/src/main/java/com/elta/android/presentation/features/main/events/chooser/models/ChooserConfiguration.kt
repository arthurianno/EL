package com.elta.android.presentation.features.main.events.chooser.models

import android.os.Parcelable
import com.elta.android.domain.features.diary.chooser.model.ChooserType
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.MedicamentInsulinType
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChooserConfiguration(
    val chooserType: ChooserType,
    val eventType: EventType,
    val id: String? = null,
    val medicament: MedicamentChooser? = null,
) : Parcelable
