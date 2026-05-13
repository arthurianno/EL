package com.elta.android.data.features.diary.events.api

import com.elta.android.data.features.diary.events.dto.SimpleEventDto
import com.elta.android.data.features.diary.events.dto.v2.EventV2RequestDto
import com.elta.android.data.features.diary.events.dto.v2.EventV2Dto
import com.elta.android.data.features.diary.events.dto.v2.EventsV2Dto
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface EventsV2Api {

    @GET("api/diary/events/v2")
    fun getEvents(
        @Query("touchedAfter") touchedAfter: Long?,
        @Query("ignoreDeleted") ignoreDeleted: Boolean,
        @Query("pageIndex") page: Int,
        @Query("pageSize") pageSize: Int,
        @Query("languageTag") languageTag: String
    ): Observable<EventsV2Dto>

    @POST("api/diary/events/v2")
    fun addEvents(
        @Query("sendToRostech") sendToRostech: Boolean,
        @Query("languageTag") languageTag: String,
        @Body events: List<EventV2RequestDto>
    ): Observable<List<EventV2Dto>>

    @POST("api/diary/events/v2")
    suspend fun addEventsSuspend(
        @Query("sendToRostech") sendToRostech: Boolean,
        @Query("languageTag") languageTag: String,
        @Body events: List<EventV2RequestDto>
    ): List<EventV2Dto>

    @PUT("api/diary/events/v2")
    fun updateEvents(
        @Query("languageTag") languageTag: String,
        @Body events: List<EventV2RequestDto>
    ): Observable<List<EventV2Dto>>

    @HTTP(method = "DELETE", path = "api/diary/events/v2", hasBody = true)
    fun deleteEvents(
        @Query("languageTag") languageTag: String,
        @Body events: List<SimpleEventDto>
    ): Observable<List<EventV2Dto>>
}
