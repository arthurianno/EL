package com.elta.android.data.features.observers.dto

import com.elta.android.data.features.common.dto.DataWithStateDto
import com.elta.android.data.features.common.dto.StateDto
import com.google.gson.annotations.SerializedName

data class ObserverDto(
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String?,
    @SerializedName("status") val status: ObserverStatusDto,
    @SerializedName("timeStamp") val modificationTime: Long?,
    @SerializedName("id") override val id: String,
    @SerializedName("modifiedState") override val state: StateDto
) : DataWithStateDto