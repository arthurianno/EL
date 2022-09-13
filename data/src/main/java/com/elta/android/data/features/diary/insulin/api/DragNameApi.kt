package com.elta.android.data.features.diary.insulin.api

import com.elta.android.data.features.diary.insulin.dto.DrugDto
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface DragNameApi {

    @GET("api/diary/v1/events/medicaments")
    fun getDrugNames(
        @Query("insulinType") insulinType: String
    ): Observable<List<DrugDto>>
}
