package com.elta.android.domain.features.multiLang.usecases

import com.elta.android.domain.features.multiLang.repositories.ScreenConfigRepository
import javax.inject.Inject

/**
 * Use case to load background image for a URL.
 * Uses cache if available, else fetches.
 */
class LoadBackgroundImageUseCase @Inject constructor(
    private val repository: ScreenConfigRepository
) {
    suspend operator fun invoke(url: String): ByteArray? {
        // In real: check local cache first, else fetch and cache
        return repository.getBackgroundImage(url)
    }
}