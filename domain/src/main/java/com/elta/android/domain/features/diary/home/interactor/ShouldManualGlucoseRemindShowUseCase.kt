package com.elta.android.domain.features.diary.home.interactor

import com.elta.android.domain.features.appsettings.AppSettingsRepository
import com.nullgr.core.interactor.SingleUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Single
import javax.inject.Inject

class ShouldManualGlucoseRemindShowUseCase @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository,
    schedulers: SchedulersFacade
) : SingleUseCase<Boolean, Unit>(schedulers) {
    override fun buildUseCaseObservable(params: Unit?): Single<Boolean> {
        return Single.just(
            appSettingsRepository.shouldManualGlucoseDialogShow
        )
    }
}