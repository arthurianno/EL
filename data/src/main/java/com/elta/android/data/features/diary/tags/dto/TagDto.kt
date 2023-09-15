package com.elta.android.data.features.diary.tags.dto

import com.elta.android.data.features.common.dto.DataWithStateDto
import com.elta.android.data.features.common.dto.StateDto
import com.google.gson.annotations.SerializedName

data class TagDto(
    @SerializedName("foodName") val name: String,
    @SerializedName("image") val image: TagImageDto,
    @SerializedName("isReadOnly") val isReadOnly: Boolean,
    @SerializedName("timeStamp") val modificationTime: Long?,
    @SerializedName("id") override val id: String,
    @SerializedName("modifiedState") override val state: StateDto
) : DataWithStateDto
