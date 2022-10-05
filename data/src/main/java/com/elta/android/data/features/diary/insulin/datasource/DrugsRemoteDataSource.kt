package com.elta.android.data.features.diary.insulin.datasource

import com.elta.android.data.features.diary.insulin.api.DrugNameApi
import com.elta.android.data.features.diary.insulin.cache.DrugCachedDto
import com.elta.android.domain.features.diary.events.model.InsulinType
import javax.inject.Inject

class DrugsRemoteDataSource @Inject constructor(
    private val api: DrugNameApi
) : DrugsDataSource {

    override fun getDrugNames(type: InsulinType) = api.getDrugNames(type.name)
    override fun clearDrugs(type: InsulinType?) {}

    override fun saveDrugs(drugs: List<DrugCachedDto>) {}
}
