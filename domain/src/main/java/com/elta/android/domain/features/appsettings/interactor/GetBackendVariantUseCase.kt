package com.elta.android.domain.features.appsettings.interactor

import com.elta.android.common.BuildConfig
import com.elta.android.domain.features.appsettings.AppSettingsRepository
import com.elta.android.domain.features.appsettings.model.BackendVariant
import com.nullgr.core.interactor.SingleUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Single
import javax.inject.Inject

class GetBackendVariantUseCase @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository,
    schedulers: SchedulersFacade
) : SingleUseCase<BackendVariant, Unit>(schedulers) {

    override fun buildUseCaseObservable(params: Unit?): Single<BackendVariant> =
        if (BuildConfig.DEBUG) appSettingsRepository.getBackendVariant()
        else Single.fromCallable { BackendVariant.PROD }
}
