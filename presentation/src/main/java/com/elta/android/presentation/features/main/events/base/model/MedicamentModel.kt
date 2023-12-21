package com.elta.android.presentation.features.main.events.base.model

import com.elta.android.domain.features.diary.medicines.model.Medicament

data class MedicamentModel(
    val medicament: Medicament?,
    val fromEvent: Boolean,
    val otherName: String? = null,
)