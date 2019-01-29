package com.elta.android.presentation.utils.dynamic_links

import android.net.Uri
import com.elta.android.presentation.Screens
import ru.terrakok.cicerone.android.support.SupportAppScreen

object DynamicLinkNavigationMapper {

    private const val RESET_PASSWORD_PATH = "resetpassword"
    private const val VERIFY_EMAIL_PATH = "emailconfirmed"
    private const val QUERY_TOKEN = "token"

    fun deepLinkToScreen(uri: Uri): SupportAppScreen? {
        val token = uri.getQueryParameter(QUERY_TOKEN)
        return when {
            token != null && uri.lastPathSegment.equals(RESET_PASSWORD_PATH, true) ->
                Screens.PasswordCreate(token)
            token != null && uri.lastPathSegment.equals(VERIFY_EMAIL_PATH, true) ->
                Screens.EmailConfirmation(token)
            else -> null
        }
    }
}