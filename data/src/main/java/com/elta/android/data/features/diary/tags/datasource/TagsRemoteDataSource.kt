package com.elta.android.data.features.diary.tags.datasource

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.common.checkNetwork
import com.elta.android.data.features.common.cache.updateCache
import com.elta.android.data.features.common.isTheLastPage
import com.elta.android.data.features.common.storage.SyncStorage
import com.elta.android.data.features.diary.tags.api.TagsApi
import com.elta.android.data.features.diary.tags.cache.TagsCache
import com.elta.android.data.features.diary.tags.cache.dto.TagCachedDto
import com.elta.android.data.features.diary.tags.dto.TagDto
import com.elta.android.data.features.diary.tags.dto.TagsDto
import com.nullgr.core.date.toTimestamp
import com.nullgr.core.hardware.NetworkChecker
import io.reactivex.Observable
import java.util.Date
import javax.inject.Inject

class TagsRemoteDataSource @Inject constructor(
    private val toCacheMapper: Mapper<TagDto, TagCachedDto>,
    private val cache: TagsCache,
    private val syncStorage: SyncStorage,
    private val checker: NetworkChecker,
    private val api: TagsApi
) : TagsDataSource {

    override fun getTags(): Observable<List<TagDto>> =
        getDataByPage(PAGE, PAGE_SIZE).checkNetwork(checker)
            .doOnNext { syncStorage.lastTagsSync = Date().toTimestamp() }
            .map(TagsDto::tags)
            .doOnNext { tags -> updateCache(tags, cache, toCacheMapper) }

    private fun getDataByPage(page: Int, size: Int): Observable<TagsDto> =
        api.getTags(syncStorage.lastTagsSync, page, size)
            .concatMap { data ->
                val meta = data.meta
                val nextPage = meta.currentPage + 1
                when (meta.isTheLastPage()) {
                    true -> Observable.just(data)
                    else -> Observable.just(data).concatWith(getDataByPage(nextPage, meta.pageSize))
                }
            }
            .collectInto(mutableListOf<TagsDto>()) { list, data -> list.add(data) }
            .map { list ->
                val allData = list.map { it.tags }.flatten()
                val lastMeta = list.last().meta
                TagsDto(allData, lastMeta)
            }
            .toObservable()

    private companion object {
        const val PAGE = 1
        const val PAGE_SIZE = 150
    }
}