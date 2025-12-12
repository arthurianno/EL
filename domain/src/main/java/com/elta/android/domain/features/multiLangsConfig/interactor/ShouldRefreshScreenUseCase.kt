package com.elta.android.domain.features.multiLangsConfig.interactor

import com.elta.android.domain.features.multiLangsConfig.repository.MultilangConfigRepository
import javax.inject.Inject

class ShouldRefreshScreenUseCase @Inject constructor(private val repository : MultilangConfigRepository) {
    suspend operator fun invoke() = repository.shouldRefreshScreensConfig()
}