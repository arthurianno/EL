package com.elta.android.domain.features.devices.interactor

import com.elta.android.domain.features.devices.repository.TestDeviceRepository
import com.nullgr.core.interactor.CompletableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.rx2.asObservable
import javax.inject.Inject

class TestSecondUseCase @Inject constructor(
    private val repo: TestDeviceRepository, schedulers: SchedulersFacade,
    private val dispatcher: CoroutineDispatcher
) : CompletableUseCase<Unit>(schedulers) {

    override fun buildUseCaseObservable(params: Unit?): Completable {

        return flow { emit(repo.testConnect()) }
            .asObservable()
            .ignoreElements()
    }
}