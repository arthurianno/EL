package com.elta.android.data.features.auth.storage

import android.annotation.SuppressLint
import com.elta.android.common.errors.InvalidRefreshTokenError
import com.elta.android.data.features.auth.api.TokenRefreshApi
import com.elta.android.data.features.auth.api.request.RefreshRequest
import com.nullgr.core.security.prefs.CryptoPreferences

class LocalTokenStorage(
    private val pref: CryptoPreferences,
    private val api: TokenRefreshApi
) : TokenStorage {

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

    @SuppressLint("CheckResult")
    override fun refresh() {
        if (accessToken != null && refreshToken != null) {
            api.refresh(RefreshRequest(accessToken, refreshToken))
                .doOnSuccess { tokens ->
                    accessToken = tokens.accessToken
                    refreshToken = tokens.refreshToken
                }
                .blockingGet()
        } else throw InvalidRefreshTokenError("Tokens can`t be null")
    }

    private companion object {
        const val ACCESS_TOKEN = "access_token"
        const val REFRESH_TOKEN = "refresh_token"
    }
}