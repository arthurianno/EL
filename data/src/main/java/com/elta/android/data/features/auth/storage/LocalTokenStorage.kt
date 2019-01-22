package com.elta.android.data.features.auth.storage

import com.nullgr.core.security.prefs.CryptoPreferences

class LocalTokenStorage(private val pref: CryptoPreferences) : TokenStorage {

    override var accessToken: String?
        get() = pref.getString(ACCESS_TOKEN, null)
        set(value) {
            pref.setString(ACCESS_TOKEN, value)
        }

    override var refreshToken: String?
        get() = pref.getString(REFRESH_TOKEN, null)
        set(value) {
            pref.setString(REFRESH_TOKEN, value)
        }

    private companion object {
        const val ACCESS_TOKEN = "access_token"
        const val REFRESH_TOKEN = "refresh_token"
    }
}