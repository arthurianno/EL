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
import androidx.core.content.edit

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
        Log.i("MultiLangConfigRepo", "🌐 Начинаем загрузку конфигураций экранов с сервера...")

        return try {
            val networkStartTime = System.currentTimeMillis()
            val response = api.getAllScreensBySlugs()
            val networkDuration = System.currentTimeMillis() - networkStartTime

            if (response.isSuccessful && response.body() != null) {
                val lang = getSystemLanguageCode()
                val dtoList = response.body()!!.content
                val newEntities = dtoList.map { it.toRoomEntity() }

                Log.i("MultiLangConfigRepo", "✅ Загружено ${dtoList.size} экранов с сервера за ${networkDuration}ms")
                Log.i("MultiLangConfigRepo", "📋 Слаги экранов: ${dtoList.map { it.slug }.joinToString(", ")}")

                // ✅ ОПТИМИЗАЦИЯ: Поэлементное сравнение
                val dbStartTime = System.currentTimeMillis()
                val existingEntities = dao.getConfigs()
                val existingMap = existingEntities.associateBy { it.slug }

                val toUpdate = newEntities.filter { newEntity ->
                    val existing = existingMap[newEntity.slug]
                    // Обновляем если записи нет или она изменилась
                    existing == null || existing != newEntity
                }

                if (toUpdate.isNotEmpty()) {
                    dao.insertAll(toUpdate)
                    val dbDuration = System.currentTimeMillis() - dbStartTime
                    Log.i("MultiLangConfigRepo", "💾 Обновлено ${toUpdate.size} из ${newEntities.size} экранов в БД за ${dbDuration}ms")
                    Log.i("MultiLangConfigRepo", "📝 Обновленные слаги: ${toUpdate.map { it.slug }.joinToString(", ")}")
                } else {
                    Log.i("MultiLangConfigRepo", "⏭️ Изменений не обнаружено, БД не обновлялась")
                }

                // Маппим в ScreenEntity с обработкой ошибок
                Log.d("MultiLangConfigRepo", "🔄 Начинаем маппинг ${dtoList.size} экранов в ScreenEntity...")
                val screenEntities = mutableListOf<ScreenEntity>()
                dtoList.forEachIndexed { index, dto ->
                    try {
                        val entity = dto.toEntity(lang)
                        screenEntities.add(entity)
                        Log.v("MultiLangConfigRepo", "✅ Экран ${index + 1}/${dtoList.size}: slug='${dto.slug}'")
                    } catch (e: Exception) {
                        Log.e("MultiLangConfigRepo", "❌ Ошибка маппинга экрана ${index + 1}/${dtoList.size}: slug='${dto.slug}', error=${e.message}", e)
                    }
                }
                Log.i("MultiLangConfigRepo", "✅ Успешно замапили ${screenEntities.size} из ${dtoList.size} экранов")

                Resource.Success(screenEntities)
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
            Log.d("MultiLangConfigRepo", "🔍 Запрашиваем конфигурацию из кеша для слага: '$slug'")
            val cachedScreen = dao.getConfigBySlug(slug)
            if (cachedScreen != null) {
                val lang = getSystemLanguageCode()  // Получаем системный язык (ru, en и т.д.)
                val screenEntity = cachedScreen.toLocalizedScreenEntity(lang)
                Log.i("MultiLangConfigRepo", "✅ Найдена конфигурация для '$slug': title='${screenEntity.title}', lang='$lang'")
                Resource.Success(screenEntity)
            } else {
                Log.w("MultiLangConfigRepo", "⚠️ Конфигурация для слага '$slug' не найдена в кеше")
                Resource.Error(
                    message = "No cached screen config found",
                    errorType = ErrorType.NOT_FOUND
                )
            }
        } catch (e: Exception) {
            Log.e("MultiLangConfigRepo", "❌ Ошибка получения конфигурации для '$slug': ${e.message}")
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
        prefs.edit { putLong(KEY_LAST_REFRESH, System.currentTimeMillis()) }
    }
}