package com.elta.android.domain.features.remoteconfig.repository

import com.elta.android.domain.features.remoteconfig.model.FeatureConfig

interface RemoteConfigRepository {

    suspend fun fetchRemoteConfig(): Boolean
    fun getFeatureConfig(): FeatureConfig
}
