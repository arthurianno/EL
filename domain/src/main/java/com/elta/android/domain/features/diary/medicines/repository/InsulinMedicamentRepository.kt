package com.elta.android.domain.features.diary.medicines.repository

import com.elta.android.domain.features.diary.medicines.model.InsulinMedicament
import com.elta.android.domain.features.diary.medicines.model.InsulinMedicamentStatistic
import com.elta.android.domain.features.diary.medicines.model.MedicamentInsulinType
import io.reactivex.Completable
import io.reactivex.Observable

interface InsulinMedicamentRepository {

    fun getInsulinMedicaments(type: MedicamentInsulinType): Observable<List<InsulinMedicament>>
    fun getInsulinTypes(): Observable<List<MedicamentInsulinType>>
    fun getBasalAndBolusTypes(): Observable<InsulinMedicamentStatistic>
    fun sync(): Completable
    
}
