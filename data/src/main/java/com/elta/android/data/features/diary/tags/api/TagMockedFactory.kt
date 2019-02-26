package com.elta.android.data.features.diary.tags.api

import com.elta.android.data.features.common.dto.StateDto
import com.elta.android.data.features.diary.tags.dto.TagDto
import com.elta.android.data.features.diary.tags.dto.TagImageDto
import java.util.Date

object TagMockedFactory {

    val nextId: String
        get() = TagImageDto.values().random().name

    val nextImage: TagImageDto
        get() = TagImageDto.values().random()

    fun create(image: TagImageDto, name: String? = null, state: StateDto = StateDto.CREATED): TagDto =
        TagDto(
            id = name ?: image.name,
            name = name ?: image.name,
            image = image,
            isReadOnly = name == null,
            modificationTime = Date().time,
            state = state
        )
}