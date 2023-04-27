package com.elta.android.domain.common.usecase

import com.elta.android.domain.common.repository.MediaRepository
import javax.inject.Inject

class ClearCacheUseCase @Inject constructor(
    private val repository: MediaRepository
) {

    suspend operator fun invoke() {
        repository.clearCache()
    }
}