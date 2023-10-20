package com.elta.android.domain.features.diary.chooser.interactor

import com.elta.android.domain.features.diary.events.model.Medicament
import com.elta.android.domain.features.diary.events.model.MedicamentInsulinType
import com.elta.android.domain.features.diary.insulin.MedicinesRepository
import com.nullgr.core.interactor.ObservableListUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Observable
import javax.inject.Inject

class GetMedicinesChooserOptionsUseCase @Inject constructor(
    private val medicinesRepository: MedicinesRepository,
    scheduler: SchedulersFacade
): ObservableListUseCase<Medicament, GetMedicinesChooserOptionsUseCase.Params>(scheduler) {

    override fun buildUseCaseObservable(params: Params?): Observable<List<Medicament>> {
        val p = checkNotNull(params?.insulinType)
        return medicinesRepository.getMedicines(p)
    }

    data class Params(
        val insulinType: MedicamentInsulinType
    )
}