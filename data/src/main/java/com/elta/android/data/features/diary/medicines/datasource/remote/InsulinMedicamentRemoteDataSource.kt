package com.elta.android.data.features.diary.medicines.datasource.remote

import com.elta.android.data.features.diary.medicines.api.MedicinesApi
import com.elta.android.data.features.diary.medicines.dto.InsulinMedicamentsNetworkResponse
import io.reactivex.Single
import javax.inject.Inject

class InsulinMedicamentRemoteDataSource @Inject constructor(
    private val api: MedicinesApi,
) : InsulinMedicamentRemoteSource {


    override fun getInsulinMedicines(): Single<InsulinMedicamentsNetworkResponse> {
        return api.getInsulinMedicines()
    }

}