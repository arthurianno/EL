package com.elta.android.domain.features.auth.interactor

import com.elta.android.domain.features.auth.repository.AuthRepository
import com.nullgr.core.interactor.SingleUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Single
import javax.inject.Inject

class CheckTokenOwnerUseCase @Inject constructor(
    private val repository: AuthRepository,
    schedulers: SchedulersFacade
) : SingleUseCase<Boolean, CheckTokenOwnerUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: CheckTokenOwnerUseCase.Params?): Single<Boolean> {
        val p = checkNotNull(params)
        return repository.checkTokenOwner(p.token)
    }

    data class Params(val token: String)
}
