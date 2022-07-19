package com.elta.android.domain.features.diary.insulin

import com.elta.android.domain.features.diary.events.model.InsulinType
import io.reactivex.Observable

interface InsulinDrugNameRepository {
    fun getDrugNamesByInsulinType(type: InsulinType): Observable<List<String>>
}
