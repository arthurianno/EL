package com.elta.android.domain.features.user.interactor

import com.elta.android.domain.features.user.model.GlucoseFormat
import com.elta.android.domain.features.user.repository.ProfileRepository
import com.nullgr.core.interactor.SingleUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Single
import javax.inject.Inject

class GetGlucoseFormatUseCase @Inject constructor(
    private val repository: ProfileRepository,
    schedulers: SchedulersFacade
) : SingleUseCase<GlucoseFormat, Unit>(schedulers) {
    override fun buildUseCaseObservable(params: Unit?): Single<GlucoseFormat> =
        repository.getProfileSettings()
            .map { it.glucoseFormat }
}
