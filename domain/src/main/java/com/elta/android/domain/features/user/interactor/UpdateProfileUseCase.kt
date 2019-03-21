package com.elta.android.domain.features.user.interactor

import com.elta.android.domain.features.user.model.Profile
import com.elta.android.domain.features.user.repository.ProfileRepository
import com.nullgr.core.interactor.CompletableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val repository: ProfileRepository,
    schedulersFacade: SchedulersFacade
) : CompletableUseCase<UpdateProfileUseCase.Params>(schedulersFacade) {

    override fun buildUseCaseObservable(params: Params?): Completable =
        repository.updateProfile(checkNotNull(params).profile)

    data class Params(val profile: Profile)
}