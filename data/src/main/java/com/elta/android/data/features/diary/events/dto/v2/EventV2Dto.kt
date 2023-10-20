package com.elta.android.data.features.diary.events.dto.v2

import com.elta.android.data.features.common.dto.DataWithStateDto
import com.elta.android.data.features.common.dto.StateDto
import com.google.gson.annotations.SerializedName

data class EventV2Dto(
    @SerializedName("data") val data: EventDataV2Dto,
    @SerializedName("additionalTime") val additionTime: String,
    @SerializedName("tag") val tagId: String?,
    @SerializedName("note") val note: String?,
    @SerializedName("timeStamp") val modificationTime: Long?,
    @SerializedName("id") override val id: String,
    @SerializedName("modifiedState") override val state: StateDto
) : DataWithStateDto
