package com.elta.android.data.features.auth.storage

interface TokenStorage {
    var accessToken: String?

    var refreshToken: String?

    fun refresh()

    fun isUserLoggedIn(): Boolean
}
