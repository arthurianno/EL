package com.elta.android.domain.features.diary.insulin

import com.elta.android.domain.features.diary.events.model.Medicament
import com.elta.android.domain.features.diary.events.model.MedicamentInsulinStatistic
import com.elta.android.domain.features.diary.events.model.MedicamentInsulinType
import io.reactivex.Completable
import io.reactivex.Observable

interface MedicinesRepository {
    fun getMedicines(type: MedicamentInsulinType): Observable<List<Medicament>>
    fun getInsulinTypes(): Observable<List<MedicamentInsulinType>>
    fun getBasalAndBolusTypes(): Observable<MedicamentInsulinStatistic>
    fun sync(): Completable
}
