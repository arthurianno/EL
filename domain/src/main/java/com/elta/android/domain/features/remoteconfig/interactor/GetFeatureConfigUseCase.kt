package com.elta.android.domain.features.remoteconfig.interactor

import com.elta.android.domain.features.remoteconfig.model.FeatureConfig
import com.elta.android.domain.features.remoteconfig.repository.RemoteConfigRepository
import javax.inject.Inject

class GetFeatureConfigUseCase @Inject constructor(
    private val repository: RemoteConfigRepository
) {
    operator fun invoke(): FeatureConfig {
        return repository.getFeatureConfig()
    }
}
