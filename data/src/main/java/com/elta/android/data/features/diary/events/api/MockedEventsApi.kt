package com.elta.android.data.features.diary.events.api

import android.content.Context
import com.elta.android.common.utils.log
import com.elta.android.data.features.common.dto.MetaDto
import com.elta.android.data.features.common.getPage
import com.elta.android.data.features.diary.events.dto.EventDto
import com.elta.android.data.features.diary.events.dto.EventsDto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import io.reactivex.Observable
import java.io.InputStreamReader

class MockedEventsApi(private val context: Context) : EventsApi {

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

    private companion object {
        const val PAGE_SIZE = 2
    }
}