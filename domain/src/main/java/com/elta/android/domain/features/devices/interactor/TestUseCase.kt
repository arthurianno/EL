package com.elta.android.domain.features.devices.interactor

import com.elta.android.domain.features.devices.repository.TestDeviceRepository
import com.nullgr.core.interactor.ObservableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Observable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.rx2.asObservable
import javax.inject.Inject

class TestUseCase @Inject constructor(
    private val repo: TestDeviceRepository, schedulers: SchedulersFacade,
    private val dispatcher: CoroutineDispatcher
) : ObservableUseCase<List<String>, Unit>(schedulers) {

    override fun buildUseCaseObservable(params: Unit?): Observable<List<String>> {
        return repo.scan()
            .flowOn(dispatcher)
            .asObservable()
    }
}