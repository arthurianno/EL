package com.elta.android.domain.features.multiLangsConfig.interactor

import com.elta.android.domain.features.multiLangsConfig.repository.MultilangConfigRepository
import javax.inject.Inject

class GetScreenConfigFromCache @Inject constructor(private val repository: MultilangConfigRepository) {
    suspend operator fun invoke(slug: String) =
        repository.getScreenConfigFromCache(slug)
}