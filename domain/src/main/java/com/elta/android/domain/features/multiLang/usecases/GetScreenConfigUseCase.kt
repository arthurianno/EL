package com.elta.android.domain.features.multiLang.usecases

import com.elta.android.domain.features.multiLang.entities.ScreenConfig
import com.elta.android.domain.features.multiLang.repositories.ScreenConfigRepository
import javax.inject.Inject

/**
 * Use case to get config for a specific screen slug.
 * Uses cache first, falls back to default/error if not found.
 */
class GetScreenConfigUseCase @Inject constructor(
    private val repository: ScreenConfigRepository
) {
    suspend operator fun invoke(slug: String): ScreenConfig? {
        // Try from cache
        val cached = repository.getCachedScreenConfigs(listOf(slug)).firstOrNull()
        if (cached != null) return cached

        // If not found, business rule: redirect to error screen (but here just return null)
        return null
    }
}