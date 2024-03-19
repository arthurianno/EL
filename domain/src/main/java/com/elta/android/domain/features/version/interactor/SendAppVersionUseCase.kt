package com.elta.android.domain.features.version.interactor

import com.elta.android.domain.features.version.repository.VersionRepository
import com.nullgr.core.interactor.CompletableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import javax.inject.Inject

class SendAppVersionUseCase @Inject constructor(
    private val repository: VersionRepository,
    service: SchedulersFacade
) : CompletableUseCase<Unit>(service) {

    override fun buildUseCaseObservable(params:Unit?): Completable =
        repository.sendAppVersion()
}
