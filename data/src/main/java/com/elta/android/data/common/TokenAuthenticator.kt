package com.elta.android.data.common

import com.elta.android.common.errors.InvalidRefreshTokenError
import com.elta.android.data.features.auth.storage.TokenStorage
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val storage: TokenStorage
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        synchronized(TokenAuthenticator::class.java) {
            val storedToken = storage.accessToken
            val requestToken = response.request().header(AUTH_HEADER)?.removePrefix(PREFIX)?.trim()

            if (storedToken == requestToken) {
                storage.refresh()
            }

            val builder = response.request().newBuilder()
            storage.accessToken?.let { token ->
                builder.header(AUTH_HEADER, "$PREFIX $token")
            } ?: throw InvalidRefreshTokenError("${response.code()}")
            return builder.build()
        }
    }
}
