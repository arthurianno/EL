package com.elta.android.presentation.features.main.events.chooser.models

import android.os.Parcelable
import com.elta.android.domain.features.diary.events.model.MedicamentInsulinType
import kotlinx.parcelize.Parcelize

@Parcelize
data class MedicamentChooser(
    val medicamentId: Int? = null,
    val medicamentName: String? = null,
    val insulinCode: String,
    val insulinId: Int,
    val insulinName: String,
) : Parcelable