package com.elta.android.data.features.observers.api

import com.elta.android.data.features.common.dto.MetaDto
import com.elta.android.data.features.common.dto.StateDto
import com.elta.android.data.features.observers.dto.ObserverDto
import com.elta.android.data.features.observers.dto.ObserverInviteEmailRequest
import com.elta.android.data.features.observers.dto.ObserverStatusDto
import com.elta.android.data.features.observers.dto.ObserverUpdateNameRequest
import com.elta.android.data.features.observers.dto.ObserversQueryResultDto
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.util.Date

@Suppress("MagicNumber")
class MockedObserverApi : ObserverApi {

    override fun getObserverInvites(
        page: Int,
        pageSize: Int
    ): Observable<ObserversQueryResultDto> = Observable.fromCallable {
        val max = ObserverDto(
            id = "vcxz-bvxc-nbcc-mnbv",
            email = "maksim@gmail.com",
            name = "Maksim Drum",
            customName = "Some Name",
            status = ObserverStatusDto.PENDING,
            modificationTime = Date().time,
            state = StateDto.CREATED
        )
        val vet = ObserverDto(
            id = "fdas-gfsd-trwe-gfsd",
            email = "vitaliy@gmail.com",
            name = "Vitaliy Vocal",
            customName = "Some Name",
            status = ObserverStatusDto.CONFIRMED,
            modificationTime = Date().time,
            state = StateDto.CREATED
        )
        val dim = ObserverDto(
            id = "trwe-hgfd-jhgf-nbvc",
            email = "dmitriy@gmail.com",
            name = "Dmitriy Bass",
            customName = "Some Name",
            status = ObserverStatusDto.EXPIRED,
            modificationTime = Date().time,
            state = StateDto.CREATED
        )
        ObserversQueryResultDto(
            items = listOf(max, vet, dim),
            meta = MetaDto(3, 1, 25)
        )
    }

    override fun sendObserverInvite(email: ObserverInviteEmailRequest): Single<ObserverDto> =
        Single.just(
            ObserverDto(
                id = "vcxz-bvxc-nbcc-mnbv",
                email = "maksim@gmail.com",
                name = "Maksim Drum",
                customName = "Some Name",
                status = ObserverStatusDto.PENDING,
                modificationTime = Date().time,
                state = StateDto.CREATED
            )
        )

    override fun updateObserverName(id: String, name: ObserverUpdateNameRequest): Completable =
        Completable.complete()

    override fun deleteObserverInvite(id: String): Completable =
        Completable.complete()
}
