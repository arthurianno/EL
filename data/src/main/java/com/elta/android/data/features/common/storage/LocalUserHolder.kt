package com.elta.android.data.features.common.storage

import android.content.SharedPreferences
import com.nullgr.core.preferences.get
import com.nullgr.core.preferences.set
import javax.inject.Inject

class LocalUserHolder @Inject constructor(
    private val pref: SharedPreferences
) : UserHolder {

    override var currentUser: Long?
        get() = pref[CURRENT_USER]
        set(value) {
            pref[CURRENT_USER] = value
        }

    private companion object {
        const val CURRENT_USER = "current_user"
    }
}