package com.elta.android.data.features.diary.events.api

import com.elta.android.data.features.diary.events.dto.EventsDto
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface EventsApi {

    @GET("api/diary/v1/events")
    fun getEvents(
        @Query("lastTimestampSync") lastSync: Long?,
        @Query("pageIndex") page: Int,
        @Query("pageSize") pageSize: Int
    ): Observable<EventsDto>
}