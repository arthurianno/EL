package com.elta.android.data.features.common.storage

import com.elta.android.data.features.common.cache.BoxScope
import com.elta.android.data.features.common.cache.BoxStoreFactory
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DbSyncStorage @Inject constructor(
    private val factory: BoxStoreFactory
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
            appBox.put(appSyncInfo?.copy(lastSalePointsSync = value)
                ?: SyncInfoDto(lastSalePointsSync = value))
        }

    override var lastEventsSync: Long?
        get() = userSyncInfo?.lastEventsSync
        set(value) {
            userBox.put(userSyncInfo?.copy(lastEventsSync = value)
                ?: SyncInfoDto(lastEventsSync = value))
        }

    override var lastTagsSync: Long?
        get() = userSyncInfo?.lastTagsSync
        set(value) {
            userBox.put(userSyncInfo?.copy(lastTagsSync = value)
                ?: SyncInfoDto(lastTagsSync = value))
        }

    override var lastGoogleFitSync: Long?
        get() = userSyncInfo?.lastGoogleFitSync
        set(value) {
            userBox.put(userSyncInfo?.copy(lastGoogleFitSync = value)
                ?: SyncInfoDto(lastGoogleFitSync = value))
        }
}