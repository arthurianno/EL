package com.elta.android.data.features.diary.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.common.storage.UserHolder
import com.elta.android.data.features.diary.cache.dto.TagCachedDto
import com.elta.android.data.features.diary.dto.tag.TagDto
import com.nullgr.core.date.dateFromTimestamp
import javax.inject.Inject

class TagToCacheMapper @Inject constructor(
    private val userHolder: UserHolder
) : Mapper<TagDto, TagCachedDto> {

    override fun mapFromObject(source: TagDto): TagCachedDto =
        with(source) {
            TagCachedDto(
                id = id.hashCode().toLong(),
                secondaryId = id,
                userId = userHolder.currentUser,
                name = name,
                image = image.name,
                isReadOnly = isReadOnly,
                modificationTime = modificationTime?.dateFromTimestamp(),
                state = state.name
            )
        }
}