package com.elta.android.data.features.common.storage

import android.content.SharedPreferences
import com.nullgr.core.preferences.set
import javax.inject.Inject

class LocalVersionHolder @Inject constructor(
    private val preferences: SharedPreferences
) : VersionHolder {
    override var lastOptionalUpdateSync: Long?
        get() = preferences.getLong(LAST_OPTIONAL_UPDATE, 0)
        set(value) {
            preferences[LAST_OPTIONAL_UPDATE] = value
        }
}

private const val LAST_OPTIONAL_UPDATE = "last_optional_update"
