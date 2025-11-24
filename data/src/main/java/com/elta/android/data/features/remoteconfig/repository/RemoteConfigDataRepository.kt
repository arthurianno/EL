package com.elta.android.data.features.remoteconfig.repository

import com.elta.android.data.features.remoteconfig.datasource.RemoteConfigManager
import com.elta.android.domain.features.remoteconfig.model.FeatureConfig
import com.elta.android.domain.features.remoteconfig.repository.RemoteConfigRepository
import javax.inject.Inject

class RemoteConfigDataRepository @Inject constructor(
    private val manager: RemoteConfigManager
) : RemoteConfigRepository {
    override suspend fun fetchRemoteConfig(): Boolean {
        return manager.fetchAndActivate()
    }

    override fun getFeatureConfig(): FeatureConfig {
        val recoveryAccount = manager.getBoolean(RECOVERY_ACCOUNT_KEY)
        val improvedEnablingLocation = manager.getBoolean(IMPROVED_ENABLING_LOCATION_KEY)

        return FeatureConfig(
            recoveryAccount = recoveryAccount,
            improvedEnablingLocation = improvedEnablingLocation
        )
    }

    /**
     * Для того чтобы добавить новые ключи, необходимо создать параметр в Remote Config в консоли Firebase,
     * скачать remote_config_defaults.xml и обновить этот файл в ресурсах. Из него можно брать ключи в константы.
     */
    companion object ConfigKey {
        private const val RECOVERY_ACCOUNT_KEY = "recovery_account"
        private const val IMPROVED_ENABLING_LOCATION_KEY = "improved_enabling_location"
    }
}
