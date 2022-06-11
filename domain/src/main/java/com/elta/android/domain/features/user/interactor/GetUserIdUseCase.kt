package com.elta.android.domain.features.user.interactor

import com.elta.android.domain.features.user.repository.ProfileRepository
import com.nullgr.core.interactor.SingleUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Single
import javax.inject.Inject

class GetUserIdUseCase @Inject constructor(
    private val userRepo: ProfileRepository,
    schedulers: SchedulersFacade
) : SingleUseCase<String, Unit>(schedulers) {

    override fun buildUseCaseObservable(params: Unit?): Single<String> =
        userRepo.getUserId()
}
