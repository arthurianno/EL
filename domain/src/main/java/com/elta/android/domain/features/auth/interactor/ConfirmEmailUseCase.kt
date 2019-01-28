package com.elta.android.domain.features.auth.interactor

import com.elta.android.domain.features.auth.repository.AuthRepository
import com.nullgr.core.interactor.CompletableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import javax.inject.Inject

class ConfirmEmailUseCase @Inject constructor(
    private val repository: AuthRepository,
    schedulers: SchedulersFacade
) : CompletableUseCase<ConfirmEmailUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: ConfirmEmailUseCase.Params?): Completable {
        val p = checkNotNull(params)
        return repository.confirmEmail(p.token)
    }

    data class Params(val token: String)
}