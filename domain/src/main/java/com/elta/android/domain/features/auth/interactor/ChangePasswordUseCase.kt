package com.elta.android.domain.features.auth.interactor

import com.elta.android.domain.features.auth.repository.AuthRepository
import com.nullgr.core.interactor.CompletableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import javax.inject.Inject

class ChangePasswordUseCase @Inject constructor(
    private val repository: AuthRepository,
    schedulers: SchedulersFacade
) : CompletableUseCase<ChangePasswordUseCase.Params>(schedulers) {
    override fun buildUseCaseObservable(params: Params?): Completable =
        checkNotNull(params).run {
            repository.changePassword(params.oldPassword, params.newPassword)
        }

    data class Params(val oldPassword: String, val newPassword: String)
}