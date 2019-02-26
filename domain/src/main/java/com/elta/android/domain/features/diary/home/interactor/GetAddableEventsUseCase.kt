package com.elta.android.domain.features.diary.home.interactor

import com.elta.android.domain.features.diary.events.model.EventType
import com.nullgr.core.interactor.SingleListUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Single
import javax.inject.Inject

class GetAddableEventsUseCase @Inject constructor(
    schedulers: SchedulersFacade
) : SingleListUseCase<EventType, Unit>(schedulers) {
    override fun buildUseCaseObservable(params: Unit?): Single<List<EventType>> =
        Single.fromCallable {
            arrayListOf(
                EventType.BREAD,
                EventType.INSULIN,
                EventType.MEDICAMENTS,
                EventType.WEIGHT,
                EventType.ACTIVITY
            )
        }
}