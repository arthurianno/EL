package com.elta.android.data.features.diary.insulin.datasource.cache

import com.elta.android.data.features.diary.insulin.cache.DrugCachedDto
import com.elta.android.data.features.diary.insulin.dto.DrugDto
import com.elta.android.domain.features.diary.events.model.InsulinType
import io.reactivex.Observable

interface DrugsCacheSource {
    fun getDrugNames(type: InsulinType): Observable<List<DrugDto>>
    fun getAll(): Observable<List<DrugDto>>
    fun clearDrugs(type: InsulinType? = null)
    fun saveDrugs(drugs: List<DrugCachedDto>)
}
