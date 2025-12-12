package com.elta.android.domain.features.multiLangsConfig.repository

import com.elta.android.domain.features.multiLangsConfig.model.Resource
import com.elta.android.domain.features.multiLangsConfig.model.ScreenEntity

interface MultilangConfigRepository {
    suspend fun getAllScreens(): Resource<List<ScreenEntity>>
    suspend fun getScreenConfigFromCache(slug: String): Resource<ScreenEntity>

    suspend fun shouldRefreshScreensConfig(): Boolean
    suspend fun updateLastRefreshTime()
}