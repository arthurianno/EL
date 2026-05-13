package com.elta.android.data.features.diary.medicines.datasource.remote

import com.elta.android.data.features.diary.medicines.dto.InsulinMedicamentsNetworkResponse

data class InsulinMedicamentsSyncResult(
    val response: InsulinMedicamentsNetworkResponse,
    val countryCode: String
)
