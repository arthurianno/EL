package com.elta.android.domain.features.feedback.interactor

import com.elta.android.domain.features.feedback.repository.FeedbackRepository
import com.nullgr.core.interactor.CompletableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import javax.inject.Inject

class SendFeedbackUseCase @Inject constructor(
    private val repo: FeedbackRepository,
    schedulers: SchedulersFacade
) : CompletableUseCase<SendFeedbackUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Completable {
        val p = checkNotNull(params)
        return repo.sendFeedback(p.name, p.email, p.message)
    }

    data class Params(
        val name: String,
        val email: String,
        val message: String
    )
}