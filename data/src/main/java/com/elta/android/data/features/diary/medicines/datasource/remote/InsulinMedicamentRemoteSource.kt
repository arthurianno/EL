package com.elta.android.data.features.diary.medicines.datasource.remote

import io.reactivex.Single

interface InsulinMedicamentRemoteSource {

    fun getInsulinMedicines(): Single<InsulinMedicamentsSyncResult>

}
