package com.elta.android.domain.features.auth.interactor

import com.elta.android.domain.features.auth.repository.AuthRepository
import com.nullgr.core.interactor.SingleUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Single
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository,
    schedulers: SchedulersFacade
) : SingleUseCase<Boolean, LoginUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Single<Boolean> {
        val p = checkNotNull(params)
        return repository.login(p.email, p.password)
    }

    data class Params(val email: String, val password: String)
}
