package com.elta.android.data.features.common.storage

import android.content.SharedPreferences
import com.nullgr.core.preferences.get
import com.nullgr.core.preferences.set
import javax.inject.Inject

class LocalSyncStorage @Inject constructor(
    private val pref: SharedPreferences
) : SyncStorage {

    override var lastSalePointsSync: Long?
        get() = pref.getLongOrNull(LAST_SALE_POINTS_SYNC)
        set(value) {
            pref[LAST_SALE_POINTS_SYNC] = value
        }

    override var lastEventsSync: Long?
        get() = pref.getLongOrNull(LAST_EVENTS_SYNC)
        set(value) {
            pref[LAST_EVENTS_SYNC] = value
        }

    override var lastTagsSync: Long?
        get() = pref.getLongOrNull(LAST_TAGS_SYNC)
        set(value) {
            pref[LAST_TAGS_SYNC] = value
        }

    private fun SharedPreferences.getLongOrNull(key: String): Long? {
        val value: Long? = this[key]
        if (value == null || value == -1L) {
            return null
        }
        return value
    }

    private companion object {
        const val LAST_SALE_POINTS_SYNC = "last_sale_points_sync"
        const val LAST_EVENTS_SYNC = "last_events_sync"
        const val LAST_TAGS_SYNC = "last_tags_sync"
    }
}