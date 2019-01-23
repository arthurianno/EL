package com.elta.android.domain.features.auth.interactor

import com.elta.android.domain.features.auth.repository.AuthRepository
import com.nullgr.core.interactor.CompletableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import javax.inject.Inject

class ResetPasswordUseCase @Inject constructor(
    private val repository: AuthRepository,
    schedulers: SchedulersFacade
) : CompletableUseCase<ResetPasswordUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Completable {
        val p = checkNotNull(params)
        return repository.resetPassword(p.token, p.newPassword)
    }

    data class Params(val token: String, val newPassword: String)
}