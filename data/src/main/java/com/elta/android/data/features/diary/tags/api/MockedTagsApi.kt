package com.elta.android.data.features.diary.tags.api

import android.content.Context
import com.elta.android.common.utils.log
import com.elta.android.data.features.common.dto.MetaDto
import com.elta.android.data.features.common.getPage
import com.elta.android.data.features.diary.tags.dto.TagDto
import com.elta.android.data.features.diary.tags.dto.TagImageDto
import com.elta.android.data.features.diary.tags.dto.TagsDto
import io.reactivex.Observable

class MockedTagsApi(private val context: Context) : TagsApi {

    private val list: MutableList<TagDto> = mutableListOf()
    private val userTags = arrayListOf("User tag 1", "User tag 2", "User tag 3", "User tag 4")

    override fun getTags(lastSync: Long?, page: Int, pageSize: Int): Observable<TagsDto> =
        Observable.fromCallable {
            if (list.isEmpty()) {
                userTags.map { name ->
                    list.add(TagMockedFactory.create(TagMockedFactory.nextImage, name))
                }
                TagImageDto.values().map { image ->
                    list.add(TagMockedFactory.create(image))
                }
            }
            val pageOfData = list.getPage(page, pageSize)
            TagsDto(pageOfData, MetaDto(pageOfData.size, page, pageSize))
        }.log("Tags", "meta") { it.meta.toString() }
}
