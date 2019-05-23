package com.elta.android.presentation.utils.dynamic_links

import android.net.Uri
import com.elta.android.presentation.Screens
import ru.terrakok.cicerone.android.support.SupportAppScreen

object DynamicLinkNavigationMapper {

    private const val RESET_PASSWORD_PATH = "resetpassword"
    private const val VERIFY_EMAIL_PATH = "emailconfirmed"
    private const val IN_APP_MESSAGE_PATH = "inappmessagelink"
    private const val QUERY_TOKEN = "token"
    private const val QUERY_SCREEN = "screen"
    private const val SETTINGS_SCREEN = "settings"

    fun deepLinkToScreen(uri: Uri): SupportAppScreen? {
        val token = uri.getQueryParameter(QUERY_TOKEN)
        val screen = uri.getQueryParameter(QUERY_SCREEN)
        return when {
            token != null && uri.lastPathSegment.equals(RESET_PASSWORD_PATH, true) ->
                Screens.PasswordCreate(token)
            token != null && uri.lastPathSegment.equals(VERIFY_EMAIL_PATH, true) ->
                Screens.EmailConfirmation(token)
            // todo test inapp messaging(temporally solution)
            screen != null && uri.lastPathSegment.equals(IN_APP_MESSAGE_PATH, true) ->
                when (screen) {
                    SETTINGS_SCREEN -> Screens.ProfileSettings
                    else -> Screens.HomeFlow
                }
            else -> null
        }
    }
}