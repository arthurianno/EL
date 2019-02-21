package com.elta.android.data.features.diary.tags.api

import android.content.Context
import com.elta.android.data.features.diary.tags.dto.TagDto
import com.elta.android.data.features.diary.tags.dto.TagsDto
import io.reactivex.Observable

class MockedTagsApi(private val context: Context) : TagsApi {

    private val list: MutableList<TagDto> = mutableListOf()

    override fun getTags(lastSync: Long?, page: Int, pageSize: Int): Observable<TagsDto> = Observable.empty()

    private companion object {
        const val PAGE_SIZE = 2
    }
}