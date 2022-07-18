package com.elta.android.data.features.diary.insulin.datasource

import com.elta.android.data.features.diary.insulin.api.InsulinDrugNameApi
import com.elta.android.domain.features.diary.events.model.InsulinType
import javax.inject.Inject

class InsulinDrugNameRemoteDataSource @Inject constructor(private val api: InsulinDrugNameApi) :
    InsulinDrugNameDataSource {

    override fun getDrugNamesByInsulinType(type: InsulinType) = api.getDrugNamesByInsulinType(type)
}
