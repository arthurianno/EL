package com.elta.android.domain.features.diary.chooser.interactor

import com.elta.android.domain.features.diary.events.model.Drug
import com.elta.android.domain.features.diary.insulin.DrugNameRepository
import com.nullgr.core.interactor.ObservableListUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Observable
import javax.inject.Inject

class GetAllInsulinChooserOptionsUseCase @Inject constructor(
        private val insulinRepo: DrugNameRepository,
        schedulers: SchedulersFacade
) : ObservableListUseCase<Drug, Unit>(schedulers) {

    override fun buildUseCaseObservable(params: Unit?): Observable<List<Drug>> {
        return insulinRepo.getAll()
    }

}
