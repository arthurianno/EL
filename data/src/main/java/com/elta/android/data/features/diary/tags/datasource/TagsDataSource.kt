package com.elta.android.data.features.diary.tags.datasource

import com.elta.android.data.features.diary.tags.dto.TagDto
import io.reactivex.Observable

interface TagsDataSource {

    fun getTags(): Observable<List<TagDto>>
}