package com.elta.android.data.features.diary.medicines.datasource.remote

import com.elta.android.common.utils.currentMillisUtc
import com.elta.android.data.features.common.network.ApiLocaleResolver
import com.elta.android.data.features.common.storage.SyncStorage
import com.elta.android.data.features.diary.medicines.api.MedicinesApi
import com.elta.android.data.features.diary.medicines.dto.MedicamentNetworkResponse
import android.util.Log
import io.reactivex.Single
import java.util.Locale
import javax.inject.Inject

class MedicamentRemoteDataSource @Inject constructor(
    private val api: MedicinesApi,
    private val syncStorage: SyncStorage
) : MedicamentRemoteSource {

    override fun syncMedicaments(): Single<List<MedicamentNetworkResponse>> {
        val touchedAfterMs = normalizeToMillis(syncStorage.lastMedicamentSync)
        val languageTag = ApiLocaleResolver.languageTag()
        Log.i(
            TAG,
            "syncMedicaments: request /api/diary/medicaments " +
                "touchedAfter=$touchedAfterMs " +
                "languageTag=$languageTag " +
                "locale=${Locale.getDefault().toLanguageTag()} " +
                "country=${Locale.getDefault().country}"
        )
        return api.getMedicaments(
            touchedAfter = touchedAfterMs,
            languageTag = languageTag
        )
            .doOnSuccess { list ->
                val preview = list.take(5).joinToString { it.name }
                Log.i(TAG, "syncMedicaments: response size=${list.size} previewNames=$preview")
                syncStorage.lastMedicamentSync = currentMillisUtc()
            }
            .doOnError { error ->
                Log.e(TAG, "syncMedicaments: request failed", error)
            }
    }

    private fun normalizeToMillis(value: Long?): Long? {
        if (value == null) return null
        return if (value < 1_000_000_000_000L) value * 1000L else value
    }

    private companion object {
        const val TAG = "MedicamentApi"
    }
}
