package com.elta.android.data.features.diary.medicines.datasource.remote

import com.elta.android.data.features.common.network.ApiCountryCodeResolver
import com.elta.android.data.features.common.network.ApiLocaleResolver
import com.elta.android.data.features.common.network.CountryCodeProvider
import com.elta.android.data.features.diary.medicines.api.MedicinesApi
import io.reactivex.Single
import retrofit2.HttpException
import javax.inject.Inject

class InsulinMedicamentRemoteDataSource internal constructor(
    private val api: MedicinesApi,
    private val countryCodeProvider: CountryCodeProvider
) : InsulinMedicamentRemoteSource {

    @Inject
    constructor(
        api: MedicinesApi,
        countryCodeResolver: ApiCountryCodeResolver
    ) : this(
        api = api,
        countryCodeProvider = countryCodeResolver
    )

    override fun getInsulinMedicines(): Single<InsulinMedicamentsSyncResult> {
        val resolvedLanguageTag = ApiLocaleResolver.languageTag()
        val countryCode = countryCodeProvider.countryCode()
        return api.getInsulinMedicines(languageTag = resolvedLanguageTag, countryCode = countryCode)
            .onErrorResumeNext { error ->
                if (error is HttpException && error.code() == HTTP_BAD_REQUEST) {
                    api.getInsulinMedicines(languageTag = null, countryCode = countryCode)
                } else {
                    Single.error(error)
                }
            }
            .map { response ->
                InsulinMedicamentsSyncResult(
                    response = response,
                    countryCode = countryCode
                )
            }
    }

    private companion object {
        const val HTTP_BAD_REQUEST = 400
    }
}
