package com.elta.android.data.features.diary.api

import android.content.Context
import com.elta.android.common.utils.log
import com.elta.android.data.features.common.dto.MetaDto
import com.elta.android.data.features.common.getPage
import com.elta.android.data.features.diary.dto.event.EventDto
import com.elta.android.data.features.diary.dto.event.EventsDto
import com.elta.android.data.features.diary.dto.tag.TagsDto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import io.reactivex.Observable
import java.io.InputStreamReader

class MockedDiaryApi(private val context: Context) : DiaryApi {

    private val list: MutableList<EventDto> = mutableListOf()

    override fun getEvents(lastSync: Long?, page: Int, pageSize: Int): Observable<EventsDto> =
        Observable.fromCallable {
            if (list.isEmpty()) {
                val file = context.assets.open("events.json")
                val type = object : TypeToken<List<EventDto>>() {}.type
                val reader = JsonReader(InputStreamReader(file))
                list.addAll(Gson().fromJson<List<EventDto>>(reader, type))
            }

            val pageOfData = list.getPage(page, PAGE_SIZE)
            EventsDto(pageOfData, MetaDto(list.size, page, PAGE_SIZE))
        }.log("Events", "meta") { it.meta.toString() }

    override fun getTags(lastSync: Long?, page: Int, pageSize: Int): Observable<TagsDto> = Observable.empty()

    private companion object {
        const val PAGE_SIZE = 2
    }
}