package com.elta.android.data.features.diary.insulin.api

import com.elta.android.domain.features.diary.events.model.InsulinType
import io.reactivex.Observable

interface InsulinNameApi {
    fun getInsulinNameByType(type: InsulinType): Observable<List<String>>
}