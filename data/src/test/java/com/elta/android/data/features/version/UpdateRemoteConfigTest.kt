package com.elta.android.data.features.version

import com.elta.android.data.features.remoteconfig.datasource.RemoteConfigManager
import com.elta.android.data.features.remoteconfig.repository.RemoteConfigDataRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateRemoteConfigTest {
    @Test
    fun `missing parameters disable notifications`() {
        val repository = RemoteConfigDataRepository(FakeConfig(emptyMap()))
        assertFalse(repository.getAppUpdateConfig().enabled)
        assertEquals(0L, repository.getAppUpdateConfig().googlePlay.versionCode)
    }

    @Test
    fun `maps per store build numbers and URLs independently of existing feature flags`() {
        val repository = RemoteConfigDataRepository(FakeConfig(mapOf(
            "app_updates_enabled" to true,
            "android_google_latest_version_code" to 420L,
            "android_google_update_url" to "https://play.google.com/store/apps/details?id=com.elta.android",
            "android_rustore_latest_version_code" to 419L,
            "android_rustore_update_url" to "https://www.rustore.ru/catalog/app/com.elta.android",
            "recovery_account" to true,
            "improved_enabling_location" to false
        )))
        val config = repository.getAppUpdateConfig()
        assertTrue(config.enabled)
        assertEquals(420L, config.googlePlay.versionCode)
        assertEquals(419L, config.ruStore.versionCode)
        assertTrue(config.ruStore.url.contains("rustore.ru"))
        assertTrue(repository.getFeatureConfig().recoveryAccount)
        assertFalse(repository.getFeatureConfig().improvedEnablingLocation)
    }

    private class FakeConfig(private val values: Map<String, Any>) : RemoteConfigManager {
        override suspend fun fetchAndActivate() = true
        override fun getString(key: String) = values[key] as? String ?: ""
        override fun getBoolean(key: String) = values[key] as? Boolean ?: false
        override fun getLong(key: String) = values[key] as? Long ?: 0L
        override fun getDouble(key: String) = values[key] as? Double ?: 0.0
    }
}
