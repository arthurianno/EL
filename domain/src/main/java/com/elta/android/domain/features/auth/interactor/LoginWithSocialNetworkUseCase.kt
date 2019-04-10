package com.elta.android.domain.features.auth.interactor

import com.elta.android.domain.features.user.model.SocialNetworkType
import com.elta.android.domain.features.auth.repository.SocialRepository
import com.nullgr.core.interactor.SingleUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Single
import javax.inject.Inject

class LoginWithSocialNetworkUseCase @Inject constructor(
    private val repository: SocialRepository,
    schedulers: SchedulersFacade
) : SingleUseCase<Boolean, LoginWithSocialNetworkUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: LoginWithSocialNetworkUseCase.Params?): Single<Boolean> =
        repository.loginWithSocialNetwork(checkNotNull(params).network)

    data class Params(val network: SocialNetworkType)
}