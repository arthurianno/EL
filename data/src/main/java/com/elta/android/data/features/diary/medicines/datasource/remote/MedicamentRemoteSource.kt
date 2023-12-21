package com.elta.android.data.features.diary.medicines.datasource.remote

import com.elta.android.data.features.diary.medicines.dto.MedicamentNetworkResponse
import io.reactivex.Single

interface MedicamentRemoteSource {

    fun syncMedicaments(): Single<List<MedicamentNetworkResponse>>

}