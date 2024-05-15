package com.elta.android.data.features.observers.api

import com.elta.android.data.features.common.dto.MetaDto
import com.elta.android.data.features.observers.model.ObserverInviteEmailNetworkRequest
import com.elta.android.data.features.observers.model.ObserverNetworkResponse
import com.elta.android.data.features.observers.model.ObserverStatusNetworkEntity
import com.elta.android.data.features.observers.model.ObserverUpdateNameNetworkRequest
import com.elta.android.data.features.observers.model.ObserversNetworkResponse
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

@Suppress("MagicNumber")
class MockedObserverApi : ObserverApi {

    override fun getObserverInvites(
        page: Int,
        pageSize: Int
    ): Observable<ObserversNetworkResponse> = Observable.fromCallable {
        val max = ObserverNetworkResponse(
            id = "vcxz-bvxc-nbcc-mnbv",
            email = "maksim@gmail.com",
            name = "Maksim Drum",
            customName = "Some Name",
            status = ObserverStatusNetworkEntity.PENDING
        )
        val vet = ObserverNetworkResponse(
            id = "fdas-gfsd-trwe-gfsd",
            email = "vitaliy@gmail.com",
            name = "Vitaliy Vocal",
            customName = "Some Name",
            status = ObserverStatusNetworkEntity.CONFIRMED
        )
        val dim = ObserverNetworkResponse(
            id = "trwe-hgfd-jhgf-nbvc",
            email = "dmitriy@gmail.com",
            name = "Dmitriy Bass",
            customName = "Some Name",
            status = ObserverStatusNetworkEntity.EXPIRED
        )
        ObserversNetworkResponse(
            items = listOf(max, vet, dim),
            meta = MetaDto(3, 1, 25)
        )
    }

    override fun sendObserverInvite(email: ObserverInviteEmailNetworkRequest): Single<ObserverNetworkResponse> =
        Single.just(
            ObserverNetworkResponse(
                id = "vcxz-bvxc-nbcc-mnbv",
                email = "maksim@gmail.com",
                name = "Maksim Drum",
                customName = "Some Name",
                status = ObserverStatusNetworkEntity.PENDING
            )
        )

    override fun updateObserverName(
        id: String,
        name: ObserverUpdateNameNetworkRequest
    ): Completable =
        Completable.complete()

    override fun deleteObserverInvite(id: String): Completable =
        Completable.complete()
}
