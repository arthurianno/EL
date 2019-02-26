package com.elta.android.data.features.diary.tags.dto

import com.elta.android.data.features.common.dto.MetaDto
import com.google.gson.annotations.SerializedName

data class TagsDto(
    @SerializedName("data") val tags: List<TagDto>,
    @SerializedName("meta") val meta: MetaDto
)