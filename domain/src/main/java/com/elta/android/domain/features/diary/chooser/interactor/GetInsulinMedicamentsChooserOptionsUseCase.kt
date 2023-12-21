package com.elta.android.domain.features.diary.chooser.interactor

import com.elta.android.domain.features.diary.medicines.model.InsulinMedicament
import com.elta.android.domain.features.diary.medicines.model.MedicamentInsulinType
import com.elta.android.domain.features.diary.medicines.repository.InsulinMedicamentRepository
import com.nullgr.core.interactor.ObservableListUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Observable
import javax.inject.Inject

class GetInsulinMedicamentsChooserOptionsUseCase @Inject constructor(
    private val insulinMedicamentRepository: InsulinMedicamentRepository,
    scheduler: SchedulersFacade
): ObservableListUseCase<InsulinMedicament, GetInsulinMedicamentsChooserOptionsUseCase.Params>(scheduler) {

    override fun buildUseCaseObservable(params: Params?): Observable<List<InsulinMedicament>> {
        val p = checkNotNull(params?.insulinType)
        return insulinMedicamentRepository.getInsulinMedicaments(p)
    }

    data class Params(
        val insulinType: MedicamentInsulinType
    )
}