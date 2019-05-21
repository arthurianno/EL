package com.elta.android.domain.features.feedback.interactor

import com.elta.android.domain.features.diary.events.repository.EventsRepository
import com.elta.android.domain.features.feedback.model.FeedbackDataModel
import com.elta.android.domain.features.feedback.repository.FeedbackRepository
import com.nullgr.core.interactor.SingleUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Single
import javax.inject.Inject

class ShouldSendFeedbackUseCase @Inject constructor(
    private val emailRepo: FeedbackRepository,
    private val eventsRepo: EventsRepository,
    schedulers: SchedulersFacade
) : SingleUseCase<FeedbackDataModel, Unit>(schedulers) {

    override fun buildUseCaseObservable(params: Unit?): Single<FeedbackDataModel> =
        emailRepo.isFeedbackWasSent()
            .flatMap { isFeedbackWasSent ->
                if (isFeedbackWasSent) {
                    Single.just(noneFeedbackModel())
                } else {
                    eventsRepo.getEvents()
                        .singleOrError()
                        .onErrorReturn { emptyList() }
                        .map { it.isFeedbackStep() }
                }
            }
}