package com.elta.android.data.features.diary.tags.datasource

import com.elta.android.data.features.diary.tags.dto.TagDto
import com.elta.android.data.features.diary.tags.api.TagsApi
import io.reactivex.Observable
import javax.inject.Inject

class TagsRemoteDataSource @Inject constructor(
    private val api: TagsApi
) : TagsDataSource {

    override fun getTags(): Observable<List<TagDto>> = Observable.empty()
}