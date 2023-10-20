package com.elta.android.data.features.diary.events.dto.v1

import com.google.gson.annotations.SerializedName

@Deprecated("use v2")
data class InsulinMedicamentDataDto(
    @SerializedName("medicament") val medicament: String?
)
