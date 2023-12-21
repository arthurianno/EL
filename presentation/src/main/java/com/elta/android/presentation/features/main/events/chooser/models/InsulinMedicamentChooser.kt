package com.elta.android.presentation.features.main.events.chooser.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class InsulinMedicamentChooser(
    val medicamentId: Int? = null,
    val medicamentName: String? = null,
    val insulinCode: String,
    val insulinId: Int,
    val insulinName: String,
) : Parcelable