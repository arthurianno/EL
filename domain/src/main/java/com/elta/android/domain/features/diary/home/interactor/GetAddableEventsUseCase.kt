package com.elta.android.domain.features.diary.home.interactor

import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.GlucoseInputType
import com.elta.android.domain.features.diary.home.model.CalculatorFlow
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
                EventType.Bread(CalculatorFlow.BREAD_UNITS),
                EventType.Glucose(GlucoseInputType.MANUAL),
                EventType.Insulin,
                EventType.Medicaments,
                EventType.Weight,
                EventType.Activity
            )
        }
}
