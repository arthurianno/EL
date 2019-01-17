package com.elta.android.domain.features.feature1.interactor

import com.elta.android.domain.features.feature1.model.TestModel
import com.elta.android.domain.features.feature1.repository.TestRepository
import com.nullgr.core.interactor.ObservableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Observable
import javax.inject.Inject

class GetTestStringUseCase @Inject constructor(
    private val repository: TestRepository,
    schedulersFacade: SchedulersFacade
) : ObservableUseCase<TestModel, GetTestStringUseCase.Params>(schedulersFacade) {

    override fun buildUseCaseObservable(params: Params?): Observable<TestModel> {
        val p = checkNotNull(params)
        return repository.getTestModel()
    }

    data class Params(val id: String)
}