package com.elta.android.data.features.diary.tags.api

import com.elta.android.data.features.diary.tags.dto.TagsDto
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface TagsApi {

    @GET("api/diary/v1/tags")
    fun getTags(
        @Query("lastTimestampSync") lastSync: Long?,
        @Query("pageIndex") page: Int,
        @Query("pageSize") pageSize: Int
    ): Observable<TagsDto>
}