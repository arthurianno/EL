package com.elta.android.domain.features.devices.interactor

import com.elta.android.domain.features.devices.model.GlucometerEvent
import com.elta.android.domain.features.devices.model.GlucometerInfo
import com.elta.android.domain.features.devices.repository.DeviceInfoRepository
import com.nullgr.core.interactor.CompletableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import javax.inject.Inject

class UpdateDeviceInfoUseCase @Inject constructor(
    private val deviceInfoRepository: DeviceInfoRepository,
    schedulers: SchedulersFacade
) : CompletableUseCase<UpdateDeviceInfoUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Completable {
        return Completable.fromCallable {
            deviceInfoRepository.updateGlucometerInfo(
                glucometerInfo = checkNotNull(params).glucometerInfo,
                lastSyncedEvent = params.lastSyncedEvent
            )
        }
    }

    data class Params(val glucometerInfo: GlucometerInfo, val lastSyncedEvent: GlucometerEvent?)
}
