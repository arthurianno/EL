package com.elta.android.data.features.diary.tags.cache

import com.elta.android.data.features.common.cache.BoxCache
import com.elta.android.data.features.common.cache.BoxStoreFactory
import com.elta.android.data.features.diary.tags.cache.dto.TagCachedDto
import javax.inject.Inject

class DbTagsCache @Inject constructor(
    factory: BoxStoreFactory
) : BoxCache<TagCachedDto>(factory), TagsCache {

    override val classToken: Class<TagCachedDto> = TagCachedDto::class.java
}