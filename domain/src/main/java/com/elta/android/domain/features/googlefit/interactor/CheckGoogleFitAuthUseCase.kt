package com.elta.android.domain.features.googlefit.interactor

import com.elta.android.domain.features.googlefit.repository.GoogleFitRepository
import com.nullgr.core.interactor.ObservableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Observable
import javax.inject.Inject

class CheckGoogleFitAuthUseCase @Inject constructor(
    private val googleFitRep: GoogleFitRepository,
    schedulersFacade: SchedulersFacade
) : ObservableUseCase<Boolean, Unit>(schedulersFacade) {

    override fun buildUseCaseObservable(params: Unit?): Observable<Boolean> =
        googleFitRep.checkAuthorization()
}
