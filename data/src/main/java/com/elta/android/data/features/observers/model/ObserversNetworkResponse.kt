package com.elta.android.data.features.observers.model

import com.elta.android.data.features.common.dto.MetaDto
import com.google.gson.annotations.SerializedName

data class ObserversNetworkResponse(
    @SerializedName("items") val items: List<ObserverNetworkResponse>,
    @SerializedName("meta") val meta: MetaDto
)
