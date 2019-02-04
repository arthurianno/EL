package com.elta.android.domain.features.user.interactor

import com.elta.android.domain.features.user.model.Diabetes
import com.elta.android.domain.features.user.model.Gender
import com.elta.android.domain.features.user.repository.UserSettingsRepository
import com.nullgr.core.interactor.CompletableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import javax.inject.Inject

class UpdateUserSettingsUseCase @Inject constructor(
    private val repository: UserSettingsRepository,
    schedulersFacade: SchedulersFacade
) : CompletableUseCase<UpdateUserSettingsUseCase.Params>(schedulersFacade) {

    override fun buildUseCaseObservable(params: Params?): Completable {
        val p = checkNotNull(params)
        return repository.updateUserProfile(p.gender, p.weight, p.diabetes)
    }

    data class Params(
        val gender: Gender,
        val weight: Float,
        val diabetes: Diabetes
    )
}