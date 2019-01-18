package com.elta.android.data.features.auth.storage

import android.content.SharedPreferences
import com.elta.android.common.utils.setNow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalTokenStorage @Inject constructor(
    private val pref: SharedPreferences
) : TokenStorage {

    override var accessToken: String?
        get() = pref.getString(ACCESS_TOKEN, null)
        set(value) {
            pref.setNow(ACCESS_TOKEN, value)
        }

    override var refreshToken: String?
        get() = pref.getString(REFRESH_TOKEN, null)
        set(value) {
            pref.setNow(REFRESH_TOKEN, value)
        }

    private companion object {
        const val ACCESS_TOKEN = "access_token"
        const val REFRESH_TOKEN = "refresh_token"
    }
}