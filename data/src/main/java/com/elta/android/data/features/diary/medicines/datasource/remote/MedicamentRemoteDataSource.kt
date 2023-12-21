package com.elta.android.data.features.diary.medicines.datasource.remote

import com.elta.android.common.utils.timestamp
import com.elta.android.data.features.common.storage.SyncStorage
import com.elta.android.data.features.diary.medicines.api.MedicinesApi
import com.elta.android.data.features.diary.medicines.dto.InsulinMedicamentsNetworkResponse
import com.elta.android.data.features.diary.medicines.dto.MedicamentNetworkResponse
import io.reactivex.Single
import javax.inject.Inject

class MedicamentRemoteDataSource @Inject constructor(
    private val api: MedicinesApi,
    private val syncStorage: SyncStorage
) : MedicamentRemoteSource {

    override fun syncMedicaments(): Single<List<MedicamentNetworkResponse>> {
        return api.getMedicaments(syncStorage.lastMedicamentSync)
            .doOnSuccess { syncStorage.lastMedicamentSync = timestamp() }
    }
}