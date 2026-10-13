package com.elta.android.domain.features.remoteconfig.repository

import com.elta.android.domain.features.remoteconfig.model.FeatureConfig
import com.elta.android.domain.features.version.model.AppUpdateConfig

interface RemoteConfigRepository {

    suspend fun fetchRemoteConfig(): Boolean
    fun getFeatureConfig(): FeatureConfig
    fun getAppUpdateConfig(): AppUpdateConfig
}
