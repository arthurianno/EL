package com.elta.android.data.features.diary.insulin.datasource.remote

import com.elta.android.data.features.diary.insulin.dto.DrugDto
import com.elta.android.domain.features.diary.events.model.InsulinType
import io.reactivex.Observable

interface DrugsRemoteSource {
    fun getDrugNames(type: InsulinType): Observable<List<DrugDto>>
}
