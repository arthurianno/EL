package com.elta.android.data.features.diary.insulin.api

import com.elta.android.data.features.diary.insulin.dto.MedicinesNetworkResponse
import io.reactivex.Single
import retrofit2.http.GET

interface MedicinesApi {
    @GET("api/diary/insulin-medicaments/v2")
    fun getInsulinMedicines(): Single<MedicinesNetworkResponse>

}