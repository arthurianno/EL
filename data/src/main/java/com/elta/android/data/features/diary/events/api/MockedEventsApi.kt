package com.elta.android.data.features.diary.events.api

import android.content.Context
import com.elta.android.common.utils.log
import com.elta.android.data.features.common.dto.MetaDto
import com.elta.android.data.features.common.dto.StateDto
import com.elta.android.data.features.common.getPage
import com.elta.android.data.features.diary.events.dto.ActivityTypeDto
import com.elta.android.data.features.diary.events.dto.EventDto
import com.elta.android.data.features.diary.events.dto.EventTypeDto
import com.elta.android.data.features.diary.events.dto.EventsDto
import com.elta.android.data.features.diary.events.dto.InsulinTypeDto
import com.elta.android.data.features.diary.events.dto.MealTagDto
import com.elta.android.data.features.diary.tags.api.TagMockedFactory
import io.reactivex.Observable

class MockedEventsApi(private val context: Context) : EventsApi {

    private val list: MutableList<EventDto> = mutableListOf()

//    override fun getEvents(lastSync: Long?, page: Int, pageSize: Int): Observable<EventsDto> =
//        Observable.fromCallable {
//            if (list.isEmpty()) {
//                val file = context.assets.open("events.json")
//                val type = object : TypeToken<List<EventDto>>() {}.type
//                val reader = JsonReader(InputStreamReader(file))
//                list.addAll(Gson().fromJson<List<EventDto>>(reader, type))
//            }
//
//            val pageOfData = list.getPage(page, PAGE_SIZE)
//            EventsDto(pageOfData, MetaDto(list.size, page, PAGE_SIZE))
//        }.log("Events", "meta") { it.meta.toString() }

    override fun getEvents(lastSync: Long?, page: Int, pageSize: Int): Observable<EventsDto> =
        Observable.fromCallable {

            if (list.isEmpty()) {
                EventTypeDto.values().forEachIndexed { index, type ->
                    (0..2).forEach { inner ->
                        list.add(
                            EventMockedFactory.create(
                                type = type,
                                value = inner * index.toDouble(),
                                activityType = if (type == EventTypeDto.ACTIVITY) ActivityTypeDto.values().random() else null,
                                mealTag = if (index % 2 == 0) MealTagDto.AFTERMEAL else MealTagDto.BEFOREMEAL,
                                insulinType = if (type == EventTypeDto.INSULIN) InsulinTypeDto.values().random() else null,
                                tagId = if (inner == 0) TagMockedFactory.nextId else null,
                                state = StateDto.values().random()
                            )
                        )
                    }
                }
            }

            val pageOfData = list.getPage(page, pageSize)
            EventsDto(pageOfData, MetaDto(list.size, page, pageSize))
        }.log("Events", "meta") { it.meta.toString() }

    private companion object {
        const val PAGE_SIZE = 2
    }
}