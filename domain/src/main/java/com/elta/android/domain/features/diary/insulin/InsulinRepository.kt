package com.elta.android.domain.features.diary.insulin

import com.elta.android.domain.features.diary.events.model.InsulinType
import io.reactivex.Observable

interface InsulinNameRepository {
    fun getInsulinNamesByType(type: InsulinType): Observable<List<String>>
}