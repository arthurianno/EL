package com.elta.android.data.features.diary.tags.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.diary.tags.dto.TagDto
import com.elta.android.domain.features.diary.tags.model.Tag
import com.elta.android.domain.features.diary.tags.model.TagImage
import com.nullgr.core.date.dateFromTimestamp
import javax.inject.Inject

class TagToDomainMapper @Inject constructor() : Mapper<TagDto, Tag> {

    override fun mapFromObject(source: TagDto): Tag =
        with(source) {
            Tag(
                id = id,
                name = name,
                image = TagImage.valueOf(image.name),
                isReadOnly = isReadOnly,
                modificationTime = modificationTime?.dateFromTimestamp()
            )
        }
}