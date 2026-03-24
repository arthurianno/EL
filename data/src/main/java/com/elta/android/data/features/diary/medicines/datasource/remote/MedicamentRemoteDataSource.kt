package com.elta.android.data.features.diary.medicines.datasource.remote

import com.elta.android.common.utils.currentMillisUtc
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
        val touchedAfterMs = normalizeToMillis(syncStorage.lastMedicamentSync)
        return api.getMedicaments(touchedAfterMs)
            .doOnSuccess { syncStorage.lastMedicamentSync = currentMillisUtc() }
    }

    private fun normalizeToMillis(value: Long?): Long? {
        if (value == null) return null
        return if (value < 1_000_000_000_000L) value * 1000L else value
    }
}