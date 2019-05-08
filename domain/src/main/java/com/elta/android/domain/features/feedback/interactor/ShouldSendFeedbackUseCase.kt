package com.elta.android.domain.features.feedback.interactor

import com.elta.android.domain.features.diary.events.repository.EventsRepository
import com.elta.android.domain.features.feedback.repository.FeedbackRepository
import com.nullgr.core.interactor.SingleUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Single
import javax.inject.Inject

class ShouldSendFeedbackUseCase @Inject constructor(
    private val emailRepo: FeedbackRepository,
    private val eventsRepo: EventsRepository,
    schedulers: SchedulersFacade
) : SingleUseCase<Boolean, Unit>(schedulers) {

    override fun buildUseCaseObservable(params: Unit?): Single<Boolean> =
        emailRepo.isFeedbackWasSent()
            .flatMap { isFeedbackWasSent ->
                if (isFeedbackWasSent) {
                    Single.just(false)
                } else {
                    eventsRepo.getEvents()
                        .singleOrError()
                        .onErrorReturn { emptyList() }
                        .map { it.isFeedbackStep() }
                }
            }
}