package com.elta.android.domain.common.usecase

import com.elta.android.domain.common.repository.ClipboardRepository
import javax.inject.Inject

class CopyTextUseCase @Inject constructor(
    private val clipboardRepository: ClipboardRepository
) {
    operator fun invoke(value: String) {
        clipboardRepository.copyText(value)
    }
}
