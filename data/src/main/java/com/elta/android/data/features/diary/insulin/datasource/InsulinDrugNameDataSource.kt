package com.elta.android.data.features.diary.insulin.datasource

import com.elta.android.domain.features.diary.events.model.InsulinType
import io.reactivex.Observable

interface InsulinDrugNameDataSource {
    fun getDrugNamesByInsulinType(type: InsulinType): Observable<List<String>>
}
