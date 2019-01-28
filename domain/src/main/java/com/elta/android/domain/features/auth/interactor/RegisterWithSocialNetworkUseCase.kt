package com.elta.android.domain.features.auth.interactor

import com.elta.android.domain.features.auth.model.SocialNetwork
import com.elta.android.domain.features.auth.repository.AuthRepository
import com.elta.android.domain.features.auth.repository.SocialRepository
import com.nullgr.core.interactor.CompletableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import javax.inject.Inject

class RegisterWithSocialNetworkUseCase @Inject constructor(
    private val authRepo: AuthRepository,
    private val socialRepo: SocialRepository,
    schedulers: SchedulersFacade
) : CompletableUseCase<RegisterWithSocialNetworkUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Completable {
        val p = checkNotNull(params)
        return authRepo.register(p.email, p.password)
            .andThen(socialRepo.linkSocialNetwork(p.network))
    }

    data class Params(val email: String, val password: String, val network: SocialNetwork)
}