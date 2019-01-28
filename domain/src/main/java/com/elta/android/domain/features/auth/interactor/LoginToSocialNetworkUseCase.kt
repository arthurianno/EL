package com.elta.android.domain.features.auth.interactor

import com.elta.android.domain.features.auth.model.SocialNetwork
import com.elta.android.domain.features.auth.repository.SocialRepository
import com.nullgr.core.interactor.CompletableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import javax.inject.Inject

class LoginToSocialNetworkUseCase @Inject constructor(
    private val repository: SocialRepository,
    schedulers: SchedulersFacade
) : CompletableUseCase<LoginToSocialNetworkUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: LoginToSocialNetworkUseCase.Params?): Completable =
        repository.loginToSocialNetwork(checkNotNull(params).network)

    data class Params(val network: SocialNetwork)
}