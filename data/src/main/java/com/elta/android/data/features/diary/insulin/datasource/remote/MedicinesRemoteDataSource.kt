package com.elta.android.data.features.diary.insulin.datasource.remote

import com.elta.android.data.features.diary.insulin.api.MedicinesApi
import com.elta.android.data.features.diary.insulin.dto.MedicinesNetworkResponse
import io.reactivex.Single
import javax.inject.Inject

class MedicinesRemoteDataSource @Inject constructor(
    private val api: MedicinesApi
): MedicinesRemoteSource {

    override fun getInsulinMedicines(): Single<MedicinesNetworkResponse> {
        return api.getInsulinMedicines()
    }
}