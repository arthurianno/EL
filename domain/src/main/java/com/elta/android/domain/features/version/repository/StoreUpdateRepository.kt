package com.elta.android.domain.features.version.repository

import com.elta.android.domain.features.version.model.AppUpdateOffer

interface StoreUpdateRepository {
    fun availableUpdates(): List<AppUpdateOffer>
    fun lastPromptTimeMillis(): Long
    fun recordPromptShown()
}
