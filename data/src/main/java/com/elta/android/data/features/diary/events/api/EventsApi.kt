package com.elta.android.data.features.diary.events.api

import com.elta.android.data.features.diary.events.dto.EventDto
import com.elta.android.data.features.diary.events.dto.EventsDto
import com.elta.android.data.features.diary.events.dto.SimpleEventDto
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface EventsApi {

    @GET("api/diary/v1/events")
    fun getEvents(
        @Query("lastTimestampSync") lastSync: Long?,
        @Query("pageIndex") page: Int,
        @Query("pageSize") pageSize: Int
    ): Observable<EventsDto>

    @POST("api/diary/v1/events")
    fun addEvents(
        @Body events: List<EventDto>
    ): Observable<List<EventDto>>

    @PUT("api/diary/v1/events")
    fun updateEvents(
        @Body events: List<EventDto>
    ): Observable<List<EventDto>>

    @HTTP(method = "DELETE", path = "api/diary/v1/events", hasBody = true)
    fun deleteEvents(
        @Body events: List<SimpleEventDto>
    ): Observable<List<EventDto>>
}
