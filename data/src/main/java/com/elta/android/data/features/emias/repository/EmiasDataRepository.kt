package com.elta.android.data.features.emias.repository

import com.elta.android.data.features.emias.api.EmiasApi
import com.elta.android.data.features.emias.mapper.toDomain
import com.elta.android.data.features.emias.mapper.toNM
import com.elta.android.domain.features.emias.model.Emias
import com.elta.android.domain.features.emias.model.EmiasStatus
import com.elta.android.domain.features.emias.repository.EmiasRepository
import io.reactivex.Completable
import javax.inject.Inject

class EmiasDataRepository @Inject constructor(
    private val emiasApi: EmiasApi
) : EmiasRepository {

    override fun updateInfo(emias: Emias): Completable {
        return emiasApi.updateEmiasInfo(emias.toNM())
    }

    override suspend fun getStatus(): Pair<EmiasStatus, Emias?> {
        return emiasApi.getEmiasStatus().toDomain()
    }

    override suspend fun unbindProfile() {
        emiasApi.unbindEmias()
    }
}
