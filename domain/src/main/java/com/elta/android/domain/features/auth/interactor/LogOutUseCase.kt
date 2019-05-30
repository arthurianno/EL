package com.elta.android.domain.features.auth.interactor

import com.elta.android.domain.features.auth.repository.AuthRepository
import com.elta.android.domain.features.auth.repository.SocialRepository
import com.elta.android.domain.features.user.model.SocialNetworkType
import com.nullgr.core.interactor.CompletableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import io.reactivex.Observable

class LogOutUseCase(
    private val authRepo: AuthRepository,
    private val socialRepo: SocialRepository,
    schedulers: SchedulersFacade
) : CompletableUseCase<Unit>(schedulers) {

    override fun buildUseCaseObservable(params: Unit?): Completable =
        authRepo.logout()
            .andThen(Observable.fromIterable(SocialNetworkType.values().asIterable())
                    .concatMapCompletable { type ->
                        socialRepo.logout(type)
                    })
}