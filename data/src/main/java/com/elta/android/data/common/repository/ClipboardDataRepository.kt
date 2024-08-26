package com.elta.android.data.common.repository

import android.content.ClipData
import android.content.ClipboardManager
import com.elta.android.domain.common.repository.ClipboardRepository
import javax.inject.Inject

class ClipboardDataRepository @Inject constructor(
    private val clipboardManager: ClipboardManager
): ClipboardRepository {
    override fun copyText(value: String) {
        val data = ClipData.newPlainText(TEXT_LABEL, value)
        clipboardManager.setPrimaryClip(data)
    }
}

private const val TEXT_LABEL = "text_label"
