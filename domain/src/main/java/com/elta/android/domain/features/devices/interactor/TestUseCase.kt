package com.elta.android.domain.features.devices.interactor

import com.elta.android.domain.features.devices.repository.DeviceRepository
import com.nullgr.core.interactor.ObservableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Observable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.rx2.asObservable
import javax.inject.Inject

class TestUseCase @Inject constructor(
    private val repo: DeviceRepository,
    schedulers: SchedulersFacade,
    private val dispatcher: CoroutineDispatcher
) : ObservableUseCase<Unit, Unit>(schedulers) {

    override fun buildUseCaseObservable(params: Unit?): Observable<Unit> {
        return flow { emit(repo.testAllDevice("C9:E8:AD:38:C5:D2", "270")) }
            .asObservable()
//        return rxObservable {
//            send(repo.testAllDevice("C9:E8:AD:38:C5:D2", "270"))
//        }
    }


}