package com.elta.android.data.features.diary.tags.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.common.dto.StateDto
import com.elta.android.data.features.diary.tags.cache.dto.TagCachedDto
import com.elta.android.data.features.diary.tags.dto.TagDto
import com.elta.android.data.features.diary.tags.dto.TagImageDto
import javax.inject.Inject

class TagFromCacheMapper @Inject constructor() : Mapper<TagCachedDto, TagDto> {

    override fun mapFromObject(source: TagCachedDto): TagDto =
        with(source) {
            TagDto(
                id = secondaryId,
                name = name,
                image = TagImageDto.valueOf(image),
                isReadOnly = isReadOnly,
                modificationTime = modificationTime,
                state = StateDto.valueOf(state)
            )
        }
}