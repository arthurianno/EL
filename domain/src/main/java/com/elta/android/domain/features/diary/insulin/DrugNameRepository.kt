package com.elta.android.domain.features.diary.insulin

import com.elta.android.domain.features.diary.events.model.InsulinType
import io.reactivex.Completable
import io.reactivex.Observable

interface DrugNameRepository {
    fun getDrugNames(type: InsulinType): Observable<List<String>>
    fun sync(): Completable
}
