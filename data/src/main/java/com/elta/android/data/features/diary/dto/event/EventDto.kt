package com.elta.android.data.features.diary.dto.event

import com.elta.android.data.features.common.dto.DataWithStateDto
import com.elta.android.data.features.common.dto.StateDto
import com.elta.android.data.features.diary.dto.EventDataDto
import com.google.gson.annotations.SerializedName

data class EventDto(
    @SerializedName("id") val id: String,
    @SerializedName("data") val data: EventDataDto,
    @SerializedName("additionalTime") val additionTime: String,
    @SerializedName("tag") val tagId: String?,
    @SerializedName("note") val note: String?,
    @SerializedName("timeStamp") val modificationTime: Long?,
    @SerializedName("state") override val state: StateDto
) : DataWithStateDto