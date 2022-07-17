package com.elta.android.data.features.diary.insulin.repository

import com.elta.android.common.di.qualifires.Remote
import com.elta.android.data.features.diary.insulin.datasource.InsulinNameDataSource
import com.elta.android.domain.features.diary.events.model.InsulinType
import com.elta.android.domain.features.diary.insulin.InsulinNameRepository
import io.reactivex.Observable
import javax.inject.Inject

class InsulinNameDataRepository @Inject constructor(
    @Remote private val remoteSource: InsulinNameDataSource
) : InsulinNameRepository {
    override fun getInsulinNamesByType(type: InsulinType): Observable<List<String>> =
        remoteSource.getInsulinNamesByType(type)
}