package com.elta.android.data.features.diary.medicines.datasource.remote

import com.elta.android.common.utils.currentMillisUtc
import com.elta.android.data.features.common.network.ApiCountryCodeResolver
import com.elta.android.data.features.common.network.ApiLocaleResolver
import com.elta.android.data.features.common.storage.SyncStorage
import com.elta.android.data.features.diary.medicines.api.MedicinesApi
import io.reactivex.Single
import javax.inject.Inject

class MedicamentRemoteDataSource @Inject constructor(
    private val api: MedicinesApi,
    private val syncStorage: SyncStorage,
    private val countryCodeResolver: ApiCountryCodeResolver
) : MedicamentRemoteSource {

    override fun syncMedicaments(): Single<MedicamentSyncResult> {
        val languageTag = ApiLocaleResolver.languageTag()
        val countryCode = countryCodeResolver.countryCode()
        val touchedAfterMs = normalizeToMillis(
            syncStorage.getLastMedicamentSync(countryCode, languageTag)
        ) ?: DEFAULT_FIRST_SYNC_CURSOR
        return api.getMedicaments(
            touchedAfter = touchedAfterMs,
            languageTag = languageTag,
            countryCode = countryCode
        )
            .doOnSuccess { list ->
                syncStorage.setLastMedicamentSync(countryCode, languageTag, currentMillisUtc())
            }
            .map { list ->
                MedicamentSyncResult(
                    medicaments = list,
                    countryCode = countryCode,
                    languageTag = languageTag
                )
            }
    }

    private fun normalizeToMillis(value: Long?): Long? {
        if (value == null) return null
        return if (value < 1_000_000_000_000L) value * 1000L else value
    }

    private companion object {
        const val DEFAULT_FIRST_SYNC_CURSOR = 0L
    }
}
