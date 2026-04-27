package com.elta.android.data.features.diary.medicines.api

import com.elta.android.data.features.diary.medicines.dto.MedicamentNetworkResponse
import com.elta.android.data.features.diary.medicines.dto.InsulinMedicamentsNetworkResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface MedicinesApi {

    @GET("api/diary/medicaments")
    fun getMedicaments(
        @Query("touchedAfter") touchedAfter: Long?,
        @Query("languageTag") languageTag: String
    ): Single<List<MedicamentNetworkResponse>>

    @GET("api/diary/insulin-medicaments/v2")
    fun getInsulinMedicines(
        @Query("languageTag") languageTag: String?
    ): Single<InsulinMedicamentsNetworkResponse>

}
