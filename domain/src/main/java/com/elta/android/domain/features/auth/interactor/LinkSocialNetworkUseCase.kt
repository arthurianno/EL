package com.elta.android.domain.features.auth.interactor

import com.elta.android.domain.features.user.model.SocialNetworkType
import com.elta.android.domain.features.auth.repository.SocialRepository
import com.nullgr.core.interactor.CompletableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import javax.inject.Inject

class LinkSocialNetworkUseCase @Inject constructor(
    private val repository: SocialRepository,
    schedulers: SchedulersFacade
) : CompletableUseCase<LinkSocialNetworkUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: LinkSocialNetworkUseCase.Params?): Completable =
        repository.linkSocialNetwork(checkNotNull(params).network)

    data class Params(val network: SocialNetworkType)
}