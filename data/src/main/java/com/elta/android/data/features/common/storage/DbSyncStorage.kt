package com.elta.android.data.features.common.storage

import android.content.Context
import com.elta.android.data.features.common.cache.BoxScope
import com.elta.android.data.features.common.cache.BoxStoreFactory
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import io.reactivex.Completable
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DbSyncStorage @Inject constructor(
    private val factory: BoxStoreFactory,
    private val context: Context
) : SyncStorage {

    private val userBox: Box<SyncInfoDto>
        get() = factory.getBoxStore(BoxScope.PER_USER).boxFor()

    private val appBox: Box<SyncInfoDto>
        get() = factory.getBoxStore(BoxScope.PER_APP).boxFor()

    private val userSyncInfo: SyncInfoDto?
        get() = userBox.all.firstOrNull()

    private val appSyncInfo: SyncInfoDto?
        get() = appBox.all.firstOrNull()

    override var lastSalePointsSync: Long?
        get() = appSyncInfo?.lastSalePointsSync
        set(value) {
            appBox.put(
                appSyncInfo?.copy(lastSalePointsSync = value)
                    ?: SyncInfoDto(lastSalePointsSync = value)
            )
        }

    override var lastEventsSync: Long?
        get() = userSyncInfo?.lastEventsSync
        set(value) {
            userBox.put(
                userSyncInfo?.copy(lastEventsSync = value)
                    ?: SyncInfoDto(lastEventsSync = value)
            )
        }

    override var lastTagsSync: Long?
        get() = userSyncInfo?.lastTagsSync
        set(value) {
            userBox.put(
                userSyncInfo?.copy(lastTagsSync = value)
                    ?: SyncInfoDto(lastTagsSync = value)
            )
        }

    override var lastGoogleFitSync: Long?
        get() = userSyncInfo?.lastGoogleFitSync
        set(value) {
            userBox.put(
                userSyncInfo?.copy(lastGoogleFitSync = value)
                    ?: SyncInfoDto(lastGoogleFitSync = value)
            )
        }

    override var lastMedicamentSync: Long?
        get() = userSyncInfo?.lastMedicamentSync
        set(value) {
            userBox.put(
                userSyncInfo?.copy(lastMedicamentSync = value)
                    ?: SyncInfoDto(lastMedicamentSync = value)
            )
        }

    private val medicamentSyncPrefs
        get() = context.getSharedPreferences(MEDICAMENT_SYNC_PREFS, Context.MODE_PRIVATE)

    override fun getLastMedicamentSync(countryCode: String, languageTag: String): Long? {
        val key = medicamentSyncKey(countryCode, languageTag)
        return medicamentSyncPrefs
            .takeIf { it.contains(key) }
            ?.getLong(key, DEFAULT_MISSING_SYNC)
            ?.takeIf { it != DEFAULT_MISSING_SYNC }
    }

    override fun setLastMedicamentSync(countryCode: String, languageTag: String, value: Long?) {
        val key = medicamentSyncKey(countryCode, languageTag)
        medicamentSyncPrefs.edit().apply {
            if (value == null) remove(key) else putLong(key, value)
        }.apply()
    }

    override fun deleteDbFiles() =
        Completable
            .fromCallable {
                medicamentSyncPrefs.edit().clear().apply()
                factory.deleteDbFiles()
            }

    private fun medicamentSyncKey(countryCode: String, languageTag: String): String {
        val country = countryCode.trim().uppercase(Locale.ROOT)
        val language = languageTag.trim().lowercase(Locale.ROOT)
        return "$MEDICAMENT_SYNC_PREFIX:$country:$language"
    }

    private companion object {
        const val MEDICAMENT_SYNC_PREFS = "medicament_sync_storage"
        const val MEDICAMENT_SYNC_PREFIX = "last_medicament_sync"
        const val DEFAULT_MISSING_SYNC = -1L
    }
}
