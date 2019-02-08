package com.elta.android.data.features.sale_points.storage

import android.content.SharedPreferences
import com.nullgr.core.preferences.get
import com.nullgr.core.preferences.set
import javax.inject.Inject

class LocalSyncStorage @Inject constructor(
    private val pref: SharedPreferences
) : SyncStorage {
    override var lastSync: Long?
        get() = pref[LAST_SYNC]
        set(value) {
            pref[LAST_SYNC] = value
        }

    private companion object {
        const val LAST_SYNC = "last_sync"
    }
}