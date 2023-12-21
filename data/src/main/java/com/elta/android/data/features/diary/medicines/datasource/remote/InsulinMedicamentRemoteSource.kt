package com.elta.android.data.features.diary.medicines.datasource.remote

import com.elta.android.data.features.diary.medicines.dto.InsulinMedicamentsNetworkResponse
import com.elta.android.data.features.diary.medicines.dto.MedicamentNetworkResponse
import io.reactivex.Single

interface InsulinMedicamentRemoteSource {

    fun getInsulinMedicines(): Single<InsulinMedicamentsNetworkResponse>

}