package com.elta.android.data.features.diary.insulin.datasource.remote

import com.elta.android.data.features.diary.insulin.api.DrugNameApi
import com.elta.android.domain.features.diary.events.model.InsulinType
import javax.inject.Inject

class DrugsRemoteDataSource @Inject constructor(
    private val api: DrugNameApi
) : DrugsRemoteSource {

    override fun getDrugNames(type: InsulinType) = api.getDrugNames(type.name)

}
