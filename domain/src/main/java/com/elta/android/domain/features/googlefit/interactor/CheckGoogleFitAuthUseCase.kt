package com.elta.android.domain.features.googlefit.interactor

import com.elta.android.domain.features.googlefit.model.GoogleFitAuthResult
import com.elta.android.domain.features.googlefit.repository.GoogleFitRepository
import com.nullgr.core.interactor.SingleUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Single
import javax.inject.Inject

class CheckGoogleFitAuthUseCase @Inject constructor(
    private val googleFitRep: GoogleFitRepository,
    schedulersFacade: SchedulersFacade
) : SingleUseCase<GoogleFitAuthResult, Unit>(schedulersFacade) {

    override fun buildUseCaseObservable(params: Unit?): Single<GoogleFitAuthResult> =
        googleFitRep.checkAuthorization()
}
