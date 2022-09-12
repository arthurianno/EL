package com.elta.android.data.features.observers.dto

import com.elta.android.data.features.common.dto.MetaDto
import com.google.gson.annotations.SerializedName

data class ObserversQueryResultDto(
    @SerializedName("items") val items: List<ObserverDto>,
    @SerializedName("meta") val meta: MetaDto
)
