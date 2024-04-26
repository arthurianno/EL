package com.elta.android.domain.features.emias.interactor

import com.elta.android.domain.features.emias.model.Emias
import com.elta.android.domain.features.emias.repository.EmiasRepository
import com.nullgr.core.interactor.CompletableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import kotlinx.coroutines.rx2.rxCompletable
import javax.inject.Inject

class UpdateEmiasUseCase @Inject constructor(
    private val emiasRepository: EmiasRepository,
    schedulers: SchedulersFacade
) : CompletableUseCase<UpdateEmiasUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Completable {
        val emias = checkNotNull(params).emias
        return emiasRepository.updateInfo(emias)
    }

    data class Params(
        val emias: Emias
    )
}
