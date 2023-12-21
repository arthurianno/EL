package com.elta.android.presentation.features.main.events.chooser.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MedicamentChooser(
    val id: Long,
    val name: String,
    val isDeleted: Boolean,
    val isOther: Boolean,
    val touchedAt: Long
) : Parcelable