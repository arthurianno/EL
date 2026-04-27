package com.elta.android.data.features.diary.medicines.datasource.remote

import com.elta.android.data.features.common.network.ApiLocaleResolver
import com.elta.android.data.features.diary.medicines.api.MedicinesApi
import com.elta.android.data.features.diary.medicines.dto.InsulinMedicamentsNetworkResponse
import android.util.Log
import io.reactivex.Single
import retrofit2.HttpException
import java.util.Locale
import javax.inject.Inject

class InsulinMedicamentRemoteDataSource @Inject constructor(
    private val api: MedicinesApi,
) : InsulinMedicamentRemoteSource {


    override fun getInsulinMedicines(): Single<InsulinMedicamentsNetworkResponse> {
        val resolvedLanguageTag = ApiLocaleResolver.languageTag()
        Log.i(
            TAG,
            "getInsulinMedicines: request /api/diary/insulin-medicaments/v2 " +
                "locale=${Locale.getDefault().toLanguageTag()} " +
                "country=${Locale.getDefault().country} " +
                "resolvedLanguageTag=$resolvedLanguageTag"
        )
        return api.getInsulinMedicines(languageTag = resolvedLanguageTag)
            .onErrorResumeNext { error ->
                if (error is HttpException && error.code() == HTTP_BAD_REQUEST) {
                    Log.w(
                        TAG,
                        "getInsulinMedicines: backend rejected languageTag=$resolvedLanguageTag, fallback without languageTag"
                    )
                    api.getInsulinMedicines(languageTag = null)
                } else {
                    Single.error(error)
                }
            }
            .doOnSuccess { response ->
                val totalItems = response.insulinMedicamentsByType.values.sumOf { it.size }
                val preview = response.insulinMedicamentsByType.entries
                    .take(3)
                    .joinToString { (type, items) ->
                        "$type=[${items.take(2).joinToString { it.name }}]"
                    }
                Log.i(TAG, "getInsulinMedicines: response totalItems=$totalItems preview=$preview")
            }
            .doOnError { error ->
                Log.e(TAG, "getInsulinMedicines: request failed", error)
            }
    }

    private companion object {
        const val TAG = "InsulinMedicamentApi"
        const val HTTP_BAD_REQUEST = 400
    }
}
