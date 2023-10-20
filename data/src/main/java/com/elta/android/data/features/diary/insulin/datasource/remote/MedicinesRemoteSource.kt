package com.elta.android.data.features.diary.insulin.datasource.remote

import com.elta.android.data.features.diary.insulin.dto.MedicinesNetworkResponse
import io.reactivex.Single

interface MedicinesRemoteSource {

    fun getInsulinMedicines(): Single<MedicinesNetworkResponse>

}