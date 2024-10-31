package com.elta.android.domain.features.diary.home.interactor

import com.elta.android.domain.features.appsettings.AppSettingsRepository
import com.nullgr.core.interactor.CompletableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import javax.inject.Inject

class SetManualGlucoseRemindUseCase @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository,
    schedulers: SchedulersFacade
) : CompletableUseCase<Boolean>(schedulers) {
    override fun buildUseCaseObservable(params: Boolean?): Completable {
        params?.let {
            appSettingsRepository.shouldManualGlucoseDialogShow = params
        }
        return Completable.complete()
    }
}
