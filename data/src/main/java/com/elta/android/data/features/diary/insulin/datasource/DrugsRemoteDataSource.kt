package com.elta.android.data.features.diary.insulin.datasource

import com.elta.android.data.features.diary.insulin.api.DragNameApi
import com.elta.android.domain.features.diary.events.model.InsulinType
import javax.inject.Inject

class DrugsRemoteDataSource @Inject constructor(
    private val api: DragNameApi
) : DrugsDataSource {

    override fun getDrugNames(type: InsulinType) = api.getDrugNames(type.name)
}
