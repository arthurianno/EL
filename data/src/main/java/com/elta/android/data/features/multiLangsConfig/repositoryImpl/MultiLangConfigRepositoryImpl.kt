package com.elta.android.data.features.multiLangsConfig.repositoryImpl


import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.elta.android.data.features.multiLangsConfig.api.MultiLangConfigApi
import com.elta.android.data.features.multiLangsConfig.mapper.ScreenMapper.toEntity
import com.elta.android.data.features.multiLangsConfig.mapper.toRoomEntity
import com.elta.android.data.features.multiLangsConfig.mapper.toLocalizedScreenEntity
import com.elta.android.data.features.multiLangsConfig.room.ScreenConfigDao
import com.elta.android.domain.features.multiLangsConfig.model.ErrorType
import com.elta.android.domain.features.multiLangsConfig.model.Resource
import com.elta.android.domain.features.multiLangsConfig.model.ScreenEntity
import com.elta.android.domain.features.multiLangsConfig.repository.MultilangConfigRepository
import javax.inject.Inject

class MultiLangConfigRepositoryImpl @Inject constructor(
    private val api: MultiLangConfigApi,
    private val dao: ScreenConfigDao,
    private val context: Context
): MultilangConfigRepository {

    companion object {
        private const val PREFS_NAME = "screen_config_prefs"
        private const val KEY_LAST_REFRESH = "last_refresh_timestamp"
        private const val TWENTY_FOUR_HOURS_MILLIS = 24 * 60 * 60 * 1000L
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    override suspend fun getAllScreens(): Resource<List<ScreenEntity>> {
        Log.e("MultiLangConfigRepo", "Fetching screens from API")

        return try {
            val response = api.getAllScreensBySlugs()

            if (response.isSuccessful && response.body() != null) {
                val lang = getSystemLanguageCode()
                val dtoList = response.body()!!.content

                // Сохраняем DTO напрямую в Room (со всеми языками)
                dao.insertAll(dtoList.map { it.toRoomEntity() })

                // Возвращаем локализованные Entity
                val screenEntity = dtoList.map { it.toEntity(lang) }
                Resource.Success(screenEntity)
            } else {
                Resource.Error(
                    message = "Failed to load screen",
                    errorType = ErrorType.NOT_FOUND
                )
            }
        } catch (e: Exception) {
            Resource.Error(
                message = e.message ?: "Unknown error",
                errorType = ErrorType.NETWORK
            )
        }
    }

    private fun getSystemLanguageCode(): String {
        val locale = java.util.Locale.getDefault()
        return locale.language
    }

    override suspend fun getScreenConfigFromCache(slug:String): Resource<ScreenEntity> {
        return try {
            val cachedScreen = dao.getConfigBySlug(slug)
            if (cachedScreen != null) {
                val lang = getSystemLanguageCode()  // Получаем системный язык (ru, en и т.д.)
                Resource.Success(cachedScreen.toLocalizedScreenEntity(lang))
            } else {
                Resource.Error(
                    message = "No cached screen config found",
                    errorType = ErrorType.NOT_FOUND
                )
            }
        } catch (e: Exception) {
            Resource.Error(
                message = e.message ?: "Unknown error",
                errorType = ErrorType.UNKNOWN
            )
        }
    }

    override suspend fun shouldRefreshScreensConfig(): Boolean {
        val lastRefresh = prefs.getLong(KEY_LAST_REFRESH, 0L)
        val currentTime = System.currentTimeMillis()

        return (currentTime - lastRefresh) >= TWENTY_FOUR_HOURS_MILLIS
    }

    override suspend fun updateLastRefreshTime() {
        prefs.edit().putLong(KEY_LAST_REFRESH, System.currentTimeMillis()).apply()
    }
}