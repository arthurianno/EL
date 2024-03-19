package com.elta.android.data.features.auth.storage

import android.annotation.SuppressLint
import com.elta.android.common.errors.InvalidRefreshTokenError
import com.elta.android.data.features.auth.api.TokenRefreshApi
import com.elta.android.data.features.auth.model.RefreshNetworkRequest
import com.nullgr.core.security.prefs.CryptoPreferences

private const val ACCESS_TOKEN = "access_token"
private const val REFRESH_TOKEN = "refresh_token"

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
            val tokens = api.refresh(RefreshNetworkRequest(accessToken, refreshToken)).execute().body()
            tokens?.let {
                accessToken = tokens.accessToken
                refreshToken = tokens.refreshToken
            }
        } else {
            throw InvalidRefreshTokenError("Tokens can`t be null")
        }
    }

    override fun isUserLoggedIn(): Boolean =
        !accessToken.isNullOrEmpty() && !refreshToken.isNullOrEmpty()
}
