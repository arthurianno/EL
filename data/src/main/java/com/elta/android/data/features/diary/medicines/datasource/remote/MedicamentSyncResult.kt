package com.elta.android.data.features.diary.medicines.datasource.remote

import com.elta.android.data.features.diary.medicines.dto.MedicamentNetworkResponse

data class MedicamentSyncResult(
    val medicaments: List<MedicamentNetworkResponse>,
    val countryCode: String,
    val languageTag: String
)
