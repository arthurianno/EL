package com.elta.android.data.features.diary.events.dto

import com.google.gson.annotations.SerializedName

data class InsulinMedicamentDataDto(
    @SerializedName("medicament") val medicament: String?
)
