package com.elta.android.domain.features.calculator.interactor

import com.elta.android.domain.features.calculator.repository.CalculatorRepository
import com.nullgr.core.interactor.CompletableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import javax.inject.Inject

class UpdateVerifiedProductUseCase @Inject constructor(
    private val repository: CalculatorRepository,
    schedulersFacade: SchedulersFacade
): CompletableUseCase<Unit>(schedulersFacade) {

    override fun buildUseCaseObservable(params: Unit?): Completable {
        return repository.updateVerifiedProducts()
    }
}
