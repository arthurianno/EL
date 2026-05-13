package com.elta.android.data.features.diary.medicines.datasource.remote

import io.reactivex.Single

interface MedicamentRemoteSource {

    fun syncMedicaments(): Single<MedicamentSyncResult>

}
