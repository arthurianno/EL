package com.elta.android.data.features.diary.insulin.repository

import com.elta.android.common.di.qualifires.Remote
import com.elta.android.data.features.diary.insulin.datasource.InsulinDrugNameDataSource
import com.elta.android.domain.features.diary.events.model.InsulinType
import com.elta.android.domain.features.diary.insulin.InsulinDrugNameRepository
import io.reactivex.Observable
import javax.inject.Inject

class InsulinDrugNameDataRepository @Inject constructor(
    @Remote private val remoteSource: InsulinDrugNameDataSource
) : InsulinDrugNameRepository {
    override fun getDrugNamesByInsulinType(type: InsulinType): Observable<List<String>> =
        remoteSource.getDrugNamesByInsulinType(type)
}
