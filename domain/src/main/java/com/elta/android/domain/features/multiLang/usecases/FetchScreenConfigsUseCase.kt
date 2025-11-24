package com.elta.android.domain.features.multiLang.usecases

import com.elta.android.domain.features.multiLang.entities.ScreenConfig
import com.elta.android.domain.features.multiLang.repositories.ScreenConfigRepository
import javax.inject.Inject

class FetchScreenConfigsUseCase @Inject constructor(
    private val repository: ScreenConfigRepository
) {
    suspend operator fun invoke(slugs: List<String>, langs: List<String>? = null): List<ScreenConfig> {
        // Business rule: If cache is valid, use cached; else fetch remote and cache.
        if (repository.isCacheValid()) {
            return repository.getCachedScreenConfigs(slugs)
        }

        // Fetch from remote
        val configs = repository.getScreenConfigs(slugs, langs)

        // Cache them
        repository.cacheScreenConfigs(configs)

        // In background: load and cache images (but since no coroutines here, assume it's handled elsewhere)
        // For simplicity, we can call it synchronously in this mock setup

        configs.forEach { config ->
            config.backgroundImageUrl?.let { url ->
                repository.getBackgroundImage(url)?.let { data ->
                    repository.cacheBackgroundImage(url, data)
                }
            }
        }

        return configs
    }
}