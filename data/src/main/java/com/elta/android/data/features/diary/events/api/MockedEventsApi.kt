package com.elta.android.data.features.diary.events.api

import com.elta.android.common.utils.log
import com.elta.android.data.features.common.dto.MetaDto
import com.elta.android.data.features.common.dto.StateDto
import com.elta.android.data.features.common.getPage
import com.elta.android.data.features.diary.events.dto.ActivityTypeDto
import com.elta.android.data.features.diary.events.dto.EventTypeDto
import com.elta.android.data.features.diary.events.dto.MealTagDto
import com.elta.android.data.features.diary.events.dto.SimpleEventDto
import com.elta.android.data.features.diary.events.dto.v2.EventV2Dto
import com.elta.android.data.features.diary.events.dto.v2.EventsV2Dto
import com.elta.android.data.features.diary.events.dto.v2.InsulinMedicamentDto
import com.elta.android.data.features.diary.tags.api.TagMockedFactory
import io.reactivex.Observable

@Suppress("MagicNumber", "ForEachOnRange", "MaxLineLength")
class MockedEventsApi : EventsV2Api {

    private val list: MutableList<EventV2Dto> = mutableListOf()

    override fun getEvents(touchedAfter: Long?, ignoreDeleted: Boolean, page: Int, pageSize: Int): Observable<EventsV2Dto> =
        Observable.fromCallable {
            if (list.isEmpty()) {
                (0..3).forEach { inner ->
                    EventTypeDto.values().forEachIndexed { index, type ->
                        list.add(
                            EventMockedFactory.create(
                                type = type,
                                value = (1..12).random().toDouble(),
                                activityType = if (type == EventTypeDto.ACTIVITY) {
                                    ActivityTypeDto.values().random()
                                } else {
                                    null
                                },
                                mealTag = if (index % 2 == 0) {
                                    MealTagDto.AFTERMEAL
                                } else {
                                    MealTagDto.BEFOREMEAL
                                },
                                insulinMedicament = if (type == EventTypeDto.INSULIN) InsulinMedicamentDto(
                                    id = 0,
                                    name = "",
                                    insulinType = InsulinMedicamentDto.MedicamentInsulinTypeDto(
                                        code = "123",
                                        id = 0,
                                        name = "name"
                                    ),
                                    deleted = false
                                ) else null,
                                tagId = if (inner == 0) {
                                    TagMockedFactory.nextId
                                } else {
                                    null
                                },
                                note = if (index % 2 == 0) "Test note" else null,
                                state = StateDto.values().random()
                            )
                        )
                        Thread.sleep(50)
                    }
                }
            }

            val pageOfData = list.getPage(page, pageSize)
            EventsV2Dto(pageOfData, MetaDto(list.size, page, pageSize))
        }.log("Events", "meta") { it.meta.toString() }

    override fun addEvents(
        sendToRostech: Boolean,
        events: List<EventV2Dto>
    ): Observable<List<EventV2Dto>> =
        Observable.just(events)

    override suspend fun addEventsSuspend(
        sendToRostech: Boolean,
        events: List<EventV2Dto>
    ): List<EventV2Dto> {
        return events
    }

    override fun updateEvents(events: List<EventV2Dto>): Observable<List<EventV2Dto>> {
        return Observable.just(events)
    }


    override fun deleteEvents(events: List<SimpleEventDto>): Observable<List<EventV2Dto>> =
        Observable.empty()
}
