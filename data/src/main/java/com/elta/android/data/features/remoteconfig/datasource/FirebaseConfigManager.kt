package com.elta.android.data.features.remoteconfig.datasource

import com.elta.android.data.BuildConfig
import com.elta.android.data.R
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseConfigManager @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig
) : RemoteConfigManager {

    override suspend fun fetchAndActivate(): Boolean {
        val updateConfigInterval = if (BuildConfig.DEBUG) 0 else HOUR_IN_SECOND

        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(updateConfigInterval)
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        return try {
            remoteConfig.fetch().await()
            remoteConfig.activate().await()
        } catch (e: Exception) {
            false
        }
    }

    override fun getString(key: String): String {
        return remoteConfig.getString(key)
    }

    override fun getBoolean(key: String): Boolean {
        return remoteConfig.getBoolean(key)
    }

    override fun getLong(key: String): Long {
        return remoteConfig.getLong(key)
    }

    override fun getDouble(key: String): Double {
        return remoteConfig.getDouble(key)
    }
}

private const val HOUR_IN_SECOND = 3600L
