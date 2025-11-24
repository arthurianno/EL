package com.elta.android.domain.features.multiLang.repositories

import com.elta.android.domain.features.multiLang.entities.ScreenConfig

interface ScreenConfigRepository {
    /**
     * Fetch screen configurations for given slugs and optional languages.
     * If langs is null, defaults to Russian only.
     * Returns list of configs, may omit unknown slugs.
     */
    suspend fun getScreenConfigs(slugs: List<String>, langs: List<String>? = null): List<ScreenConfig>

    /**
     * Get the byte array for a background image from URL.
     * This could be from cache or remote.
     */
    suspend fun getBackgroundImage(url: String): ByteArray?

    /**
     * Cache the screen configurations locally.
     * Expires after 24 hours or next successful update.
     */
    suspend fun cacheScreenConfigs(configs: List<ScreenConfig>)

    /**
     * Cache a background image locally.
     */
    suspend fun cacheBackgroundImage(url: String, data: ByteArray)

    /**
     * Check if cache is valid (not older than 24 hours).
     */
    suspend fun isCacheValid(): Boolean

    /**
     * Get cached configs for given slugs.
     */
    suspend fun getCachedScreenConfigs(slugs: List<String>): List<ScreenConfig>
}