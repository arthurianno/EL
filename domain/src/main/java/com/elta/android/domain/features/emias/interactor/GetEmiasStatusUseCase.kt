package com.elta.android.domain.features.emias.interactor

import com.elta.android.domain.features.emias.model.Emias
import com.elta.android.domain.features.emias.model.EmiasStatus
import com.elta.android.domain.features.emias.repository.EmiasRepository
import com.nullgr.core.interactor.SingleUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.rx2.rxSingle
import javax.inject.Inject
import kotlin.coroutines.EmptyCoroutineContext

class GetEmiasStatusUseCase @Inject constructor(
    private val emiasRepository: EmiasRepository,
    schedulers: SchedulersFacade
) : SingleUseCase<Pair<EmiasStatus, Emias?>, Unit>(schedulers) {
    override fun buildUseCaseObservable(params: Unit?): Single<Pair<EmiasStatus, Emias?>> {
        return rxSingle(EmptyCoroutineContext + Dispatchers.Unconfined) {
            emiasRepository.getStatus()
        }
    }
}
