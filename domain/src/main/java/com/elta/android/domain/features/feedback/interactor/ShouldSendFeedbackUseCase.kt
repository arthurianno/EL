package com.elta.android.domain.features.feedback.interactor

import com.elta.android.domain.features.diary.events.repository.EventsRepository
import com.elta.android.domain.features.feedback.model.FeedbackDataModel
import com.elta.android.domain.features.userinfo.repository.UserInfoRepository
import com.nullgr.core.interactor.SingleUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Single
import javax.inject.Inject

class ShouldSendFeedbackUseCase @Inject constructor(
    private val userInfoRepo: UserInfoRepository,
    private val eventsRepo: EventsRepository,
    schedulers: SchedulersFacade
) : SingleUseCase<FeedbackDataModel, Unit>(schedulers) {

    override fun buildUseCaseObservable(params: Unit?): Single<FeedbackDataModel> =
        userInfoRepo.getUserInfo().map { info -> info.isFeedbackSent ?: false }
            .flatMap { isFeedbackSent ->
                if (isFeedbackSent) Single.just(noneFeedbackModel())
                else eventsRepo.countEvents().map { events -> getFeedbackModel(events.toInt()) }
            }
}
