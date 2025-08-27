package com.elta.android.data.features.multiLang.repositories

import android.content.Context
import com.elta.android.data.features.multiLang.api.ConfigApi
import com.elta.android.data.features.multiLang.models.ScreenConfigResponse
import com.elta.android.data.features.multiLang.room.ScreenConfigDao
import com.elta.android.data.features.multiLang.room.ScreenConfigEntity
import com.elta.android.domain.features.multiLang.entities.MultiLangString
import com.elta.android.domain.features.multiLang.entities.ScreenConfig
import com.elta.android.domain.features.multiLang.repositories.ScreenConfigRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nullgr.core.hardware.NetworkChecker
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ScreenConfigRepositoryImpl @Inject constructor(
    private val api: ConfigApi,
    private val dao: ScreenConfigDao,
    private val gson: Gson,
    private val networkChecker: NetworkChecker,
    private val context: Context
) : ScreenConfigRepository {

    override suspend fun getScreenConfigs(slugs: List<String>, langs: List<String>?): List<ScreenConfig> {
        if (slugs.size > 200) {
            throw IllegalArgumentException("limit-exceeded")
        }
        if (networkChecker.isInternetConnectionEnabled()) {
            val response = api.getScreenConfigs(slugs, langs)
                .subscribeOn(Schedulers.io())
                .blockingGet()
            val missingSlugs = slugs.filterNot { slug -> response.any { it.slug == slug } }
            if (missingSlugs.isNotEmpty()) {
                Timber.w("Missing screen configs for slugs: $missingSlugs")
                // Можно добавить CrashlyticsReport для метрик, если внедрен
            }
            val configs = response.map { it.toDomain() }
            cacheScreenConfigs(configs) // Сохраняем в Room
            return configs
        } else {
            Timber.d("No network, using cached configs")
            return getCachedScreenConfigs(slugs)
        }
    }

    override suspend fun getBackgroundImage(url: String): ByteArray? {
        return null // Игнорируем изображения
    }

    override suspend fun cacheScreenConfigs(configs: List<ScreenConfig>) {
        val entities = configs.map { it.toEntity(gson) }
        dao.insertAll(entities)
    }

    override suspend fun cacheBackgroundImage(url: String, data: ByteArray) {
        // Игнорируем
    }

    override suspend fun isCacheValid(): Boolean {
        val lastUpdate = dao.getLastUpdateTime() ?: return false
        val twentyFourHoursMillis = TimeUnit.HOURS.toMillis(24)
        return System.currentTimeMillis() - lastUpdate < twentyFourHoursMillis
    }

    override suspend fun getCachedScreenConfigs(slugs: List<String>): List<ScreenConfig> {
        return dao.getConfigs(slugs).map { it.toDomain(gson) }
    }

    private fun ScreenConfigResponse.toDomain(): ScreenConfig {
        return ScreenConfig(
            slug = slug,
            description = MultiLangString(description),
            backgroundImageUrl = null // Игнорируем
        )
    }

    private fun ScreenConfigEntity.toDomain(gson: Gson): ScreenConfig {
        val type = object : TypeToken<Map<String, String>>() {}.type
        val translations: Map<String, String> = gson.fromJson(descriptionJson, type)
        return ScreenConfig(
            slug = slug,
            description = MultiLangString(translations),
            backgroundImageUrl = null // Игнорируем
        )
    }

    private fun ScreenConfig.toEntity(gson: Gson): ScreenConfigEntity {
        return ScreenConfigEntity(
            slug = slug,
            descriptionJson = gson.toJson(description.translations),
            backgroundImageUrl = null, // Игнорируем
            lastUpdated = System.currentTimeMillis()
        )
    }
}