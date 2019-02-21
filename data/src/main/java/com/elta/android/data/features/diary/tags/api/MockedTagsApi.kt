package com.elta.android.data.features.diary.tags.api

import android.content.Context
import com.elta.android.common.utils.log
import com.elta.android.data.features.common.dto.MetaDto
import com.elta.android.data.features.common.getPage
import com.elta.android.data.features.diary.tags.dto.TagDto
import com.elta.android.data.features.diary.tags.dto.TagsDto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import io.reactivex.Observable
import java.io.InputStreamReader

class MockedTagsApi(private val context: Context) : TagsApi {

    private val list: MutableList<TagDto> = mutableListOf()

    override fun getTags(lastSync: Long?, page: Int, pageSize: Int): Observable<TagsDto> =
        Observable.fromCallable {
            if (list.isEmpty()) {
                val file = context.assets.open("tags.json")
                val type = object : TypeToken<List<TagDto>>() {}.type
                val reader = JsonReader(InputStreamReader(file))
                list.addAll(Gson().fromJson<List<TagDto>>(reader, type))
            }

            val pageOfData = list.getPage(page, PAGE_SIZE)
            TagsDto(pageOfData, MetaDto(list.size, page, PAGE_SIZE))
        }.log("Tags", "meta") { it.meta.toString() }

    private companion object {
        const val PAGE_SIZE = 2
    }
}