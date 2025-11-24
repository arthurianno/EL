package com.elta.android.data.features.emias.api

import com.elta.android.common.errors.EmiasError
import com.elta.android.data.features.emias.dto.EmiasNetworkEntity
import com.elta.android.data.features.emias.dto.EmiasStatusResponse
import io.reactivex.Completable
import retrofit2.Response

class EmiasMockedApi : EmiasApi {
    override fun updateEmiasInfo(emias: EmiasNetworkEntity): Completable {
        return Completable.error(EmiasError.AgreementForEmiasUsageNotFound)
    }

    override suspend fun getEmiasStatus(): EmiasStatusResponse {
        return EmiasStatusResponse(null, false)
    }

    override suspend fun unbindEmias() : Response<Unit> {
        return Response.success(Unit)
    }
}

// Errors
// EmiasError.EMIAS_INTERNAL_ERROR -> throw EmiasError.EmiasInternalError
// EmiasError.OMS_ALREADY_LINKED -> throw EmiasError.OmsAlreadyLinked
// EmiasError.USER_IN_EMIAS_NOT_FOUND -> throw EmiasError.UserInEmiasNotFound
// EmiasError.AGREEMENT_FOR_EMIAS_USAGE_NOT_FOUND -> throw EmiasError.AgreementForEmiasUsageNotFound