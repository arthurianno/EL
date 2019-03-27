package com.elta.android.data.features.diary.tags.datasource

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.common.cache.Cache
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.diary.tags.cache.dto.TagCachedDto
import com.elta.android.data.features.diary.tags.dto.TagDto
import io.reactivex.Observable
import javax.inject.Inject

class TagsCachedDataSource @Inject constructor(
    private val fromCacheMapper: Mapper<TagCachedDto, TagDto>,
    private val cache: Cache<TagCachedDto>
) : TagsDataSource {

    override fun getTags(): Observable<List<TagDto>> =
        Observable.fromCallable {
            cache.getAll(CommonConditions.All)
        }.map(fromCacheMapper::mapFromObjects)
}