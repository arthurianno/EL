package com.elta.android.domain.features.auth.interactor

import com.elta.android.domain.features.auth.model.SocialNetwork
import com.elta.android.domain.features.auth.model.SocialUser
import com.elta.android.domain.features.auth.repository.AuthRepository
import com.nullgr.core.interactor.SingleUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Single
import javax.inject.Inject

class GetSocialUserUseCase @Inject constructor(
    private val repository: AuthRepository,
    schedulers: SchedulersFacade
): SingleUseCase<SocialUser, GetSocialUserUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Single<SocialUser> =
        repository.getSocialUser(checkNotNull(params).network)

    data class Params(val network: SocialNetwork)
}