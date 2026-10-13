package com.elta.android.data.features.remoteconfig.repository

import com.elta.android.data.features.remoteconfig.datasource.RemoteConfigManager
import com.elta.android.domain.features.remoteconfig.model.FeatureConfig
import com.elta.android.domain.features.remoteconfig.repository.RemoteConfigRepository
import com.elta.android.domain.features.version.model.AppUpdateConfig
import com.elta.android.domain.features.version.model.PublishedAppUpdate
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

    override fun getAppUpdateConfig() = AppUpdateConfig(
        enabled = manager.getBoolean(APP_UPDATES_ENABLED),
        googlePlay = PublishedAppUpdate(
            versionCode = manager.getLong(GOOGLE_LATEST_VERSION_CODE),
            url = manager.getString(GOOGLE_UPDATE_URL)
        ),
        ruStore = PublishedAppUpdate(
            versionCode = manager.getLong(RUSTORE_LATEST_VERSION_CODE),
            url = manager.getString(RUSTORE_UPDATE_URL)
        )
    )

    /**
     * Для того чтобы добавить новые ключи, необходимо создать параметр в Remote Config в консоли Firebase,
     * скачать remote_config_defaults.xml и обновить этот файл в ресурсах. Из него можно брать ключи в константы.
     */
    companion object ConfigKey {
        private const val RECOVERY_ACCOUNT_KEY = "recovery_account"
        private const val IMPROVED_ENABLING_LOCATION_KEY = "improved_enabling_location"
        private const val APP_UPDATES_ENABLED = "app_updates_enabled"
        private const val GOOGLE_LATEST_VERSION_CODE = "android_google_latest_version_code"
        private const val GOOGLE_UPDATE_URL = "android_google_update_url"
        private const val RUSTORE_LATEST_VERSION_CODE = "android_rustore_latest_version_code"
        private const val RUSTORE_UPDATE_URL = "android_rustore_update_url"
    }
}
