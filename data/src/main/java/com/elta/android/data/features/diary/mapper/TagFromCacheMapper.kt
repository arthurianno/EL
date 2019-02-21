package com.elta.android.data.features.diary.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.common.dto.StateDto
import com.elta.android.data.features.diary.cache.dto.TagCachedDto
import com.elta.android.data.features.diary.dto.tag.TagDto
import com.elta.android.data.features.diary.dto.tag.TagImageDto
import com.nullgr.core.date.toTimestamp
import javax.inject.Inject

class TagFromCacheMapper @Inject constructor() : Mapper<TagCachedDto, TagDto> {

    override fun mapFromObject(source: TagCachedDto): TagDto =
        with(source) {
            TagDto(
                id = secondaryId,
                name = name,
                image = TagImageDto.valueOf(image),
                isReadOnly = isReadOnly,
                modificationTime = modificationTime?.toTimestamp(),
                state = StateDto.valueOf(state)
            )
        }
}