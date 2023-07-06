package com.elta.android.domain.features.diary.events.interactor

import android.net.Uri
import com.elta.android.domain.features.diary.events.repository.EventsRepository
import com.elta.android.domain.features.diary.home.model.GlucoseSharingInfo
import com.nullgr.core.interactor.SingleUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Single
import javax.inject.Inject

class GetShareEventUriUseCase @Inject constructor(
    private val eventsRepo: EventsRepository,
    schedulers: SchedulersFacade
) : SingleUseCase<Uri, GetShareEventUriUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Single<Uri> {
        val p = checkNotNull(params)
        return eventsRepo.getShareEventUri(p.sharingInfo)
    }

    data class Params(val sharingInfo: GlucoseSharingInfo)
}
