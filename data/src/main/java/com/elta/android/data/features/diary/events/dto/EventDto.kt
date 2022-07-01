package com.elta.android.data.features.diary.events.dto

import com.elta.android.data.features.common.dto.DataWithStateDto
import com.elta.android.data.features.common.dto.StateDto
import com.google.gson.annotations.SerializedName

data class EventDto(
    @SerializedName("data") val data: EventDataDto,
    @SerializedName("additionalTime") val additionTime: String,
    @SerializedName("tag") val tagId: String?,
    @SerializedName("note") val note: String?,
    @SerializedName("timeStamp") val modificationTime: Long?,
    @SerializedName("id") override val id: String,
    @SerializedName("modifiedState") override val state: StateDto
) : DataWithStateDto
