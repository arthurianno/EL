package com.elta.android.data.features.diary.tags.repository

import com.elta.android.common.di.qualifires.Cache
import com.elta.android.common.di.qualifires.Remote
import com.elta.android.common.mapper.Mapper
import com.elta.android.data.common.onConnectionErrorReturnsEmpty
import com.elta.android.data.features.diary.tags.datasource.TagsDataSource
import com.elta.android.data.features.diary.tags.dto.TagDto
import com.elta.android.domain.features.diary.tags.model.Tag
import com.elta.android.domain.features.diary.tags.repository.TagsRepository
import io.reactivex.Observable
import javax.inject.Inject

class TagsDataRepository @Inject constructor(
    private val toDomainMapper: Mapper<TagDto, Tag>,
    @Remote private val remoteSource: TagsDataSource,
    @Cache private val cacheSource: TagsDataSource
) : TagsRepository {

    override fun getTags(): Observable<List<Tag>> =
        remoteSource.getTags()
            .onConnectionErrorReturnsEmpty()
            .flatMap { cacheSource.getTags() }
            .map(toDomainMapper::mapFromObjects)
}