package com.elta.android.data.features.diary.api

import com.elta.android.data.features.diary.dto.event.EventsDto
import com.elta.android.data.features.diary.dto.tag.TagsDto
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface DiaryApi {

    @GET("api/diary/v1/events")
    fun getEvents(
        @Query("lastTimestampSync") lastSync: Long?,
        @Query("pageIndex") page: Int,
        @Query("pageSize") pageSize: Int
    ): Observable<EventsDto>

    @GET("api/diary/v1/tags")
    fun getTags(
        @Query("lastTimestampSync") lastSync: Long?,
        @Query("pageIndex") page: Int,
        @Query("pageSize") pageSize: Int
    ): Observable<TagsDto>
}