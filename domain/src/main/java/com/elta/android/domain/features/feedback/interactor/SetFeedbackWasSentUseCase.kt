package com.elta.android.domain.features.feedback.interactor

import com.elta.android.domain.features.feedback.repository.FeedbackRepository
import com.nullgr.core.interactor.CompletableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import javax.inject.Inject

class SetFeedbackWasSentUseCase @Inject constructor(
    private val repo: FeedbackRepository,
    schedulers: SchedulersFacade
) : CompletableUseCase<Unit>(schedulers) {

    override fun buildUseCaseObservable(params: Unit?): Completable =
        repo.setFeedbackWasSent()
}