package com.elta.android.data.features.diary.insulin.datasource

import com.elta.android.data.features.diary.insulin.api.InsulinNameApi
import com.elta.android.domain.features.diary.events.model.InsulinType
import javax.inject.Inject

class InsulinNameRemoteDataSource
@Inject constructor(private val api: InsulinNameApi) : InsulinNameDataSource {
    override fun getInsulinNamesByType(type: InsulinType) = api.getInsulinNameByType(type)
}