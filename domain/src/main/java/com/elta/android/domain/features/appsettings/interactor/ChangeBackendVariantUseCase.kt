package com.elta.android.domain.features.appsettings.interactor

import com.elta.android.domain.features.appsettings.AppSettingsRepository
import com.elta.android.domain.features.appsettings.model.BackendVariant
import com.nullgr.core.interactor.CompletableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import javax.inject.Inject

class ChangeBackendVariantUseCase @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository,
    schedulers: SchedulersFacade
) : CompletableUseCase<ChangeBackendVariantUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Completable =
        appSettingsRepository
            .changeBackendVariant(checkNotNull(params).variant)
            .andThen(appSettingsRepository.deleteDbFiles())

    data class Params(val variant: BackendVariant)
}
