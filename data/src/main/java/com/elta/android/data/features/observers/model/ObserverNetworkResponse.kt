package com.elta.android.data.features.observers.model

import com.elta.android.data.features.common.dto.DataWithStateDto
import com.elta.android.data.features.common.dto.StateDto
import com.google.gson.annotations.SerializedName

data class ObserverNetworkResponse(
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String?,
    @SerializedName("customName") val customName: String?,
    @SerializedName("status") val status: ObserverStatusNetworkEntity,
    @SerializedName("timeStamp") val modificationTime: Long?,
    @SerializedName("id") override val id: String,
    @SerializedName("modifiedState") override val state: StateDto
) : DataWithStateDto
