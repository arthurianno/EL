package com.elta.android.domain.features.devices.interactor

import com.elta.android.domain.features.devices.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FindGlucometerUseCase @Inject constructor(
    private val repository: DeviceRepository
) {
    operator fun invoke(address: String): Flow<Unit> =
        repository.findGlucometer(address)
}
