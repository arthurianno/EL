package com.elta.android.domain.features.emias.interactor

import com.elta.android.domain.features.glucometers.repository.GlucometersRepository
import com.nullgr.core.interactor.CompletableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import javax.inject.Inject

class SyncGlucometersUseCase @Inject constructor(
    private val glucometersRepository: GlucometersRepository,
    schedulers: SchedulersFacade
) : CompletableUseCase<Unit>(schedulers) {
    override fun buildUseCaseObservable(params: Unit?): Completable {
        return glucometersRepository
            .sync()
            .onErrorComplete()
    }
}
