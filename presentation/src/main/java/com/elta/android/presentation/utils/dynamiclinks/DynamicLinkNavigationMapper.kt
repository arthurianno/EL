package com.elta.android.presentation.utils.dynamiclinks

import android.net.Uri
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.navigation.support.SupportAppScreen
import timber.log.Timber

object DynamicLinkNavigationMapper {

    private const val RESET_PASSWORD_PATH = "resetpassword"
    private const val VERIFY_EMAIL_PATH = "emailconfirmed"
    private const val IN_APP_MESSAGE_PATH = "inappmessagelink"
    private const val QUERY_TOKEN = "token"
    private const val QUERY_SCREEN = "screen"
    private const val SETTINGS_SCREEN = "settings"

    fun deepLinkToScreen(uri: Uri, improvedEnablingLocation: Boolean): SupportAppScreen? {
        Timber.d("🔗 DeepLink parsing: URI=$uri")
        Timber.d("🔗 Scheme: ${uri.scheme}, Host: ${uri.host}, Path: ${uri.path}")
        Timber.d("🔗 LastPathSegment: ${uri.lastPathSegment}")

        val token = uri.getQueryParameter(QUERY_TOKEN)
        val screen = uri.getQueryParameter(QUERY_SCREEN)

        Timber.d("🔗 Params: token=$token, screen=$screen")

        val result = when {
            token != null && uri.lastPathSegment.equals(RESET_PASSWORD_PATH, true) -> {
                Timber.d("✅ Matched RESET_PASSWORD_PATH -> Screens.PasswordCreate(token=$token)")
                Screens.PasswordCreate(token)
            }
            token != null && uri.lastPathSegment.equals(VERIFY_EMAIL_PATH, true) -> {
                Timber.d("✅ Matched VERIFY_EMAIL_PATH -> Screens.EmailConfirmation(token=$token)")
                Screens.EmailConfirmation(token)
            }
            // todo test inapp messaging(temporally solution)
            screen != null && uri.lastPathSegment.equals(IN_APP_MESSAGE_PATH, true) ->
                when (screen) {
                    SETTINGS_SCREEN -> {
                        Timber.d("✅ Matched IN_APP_MESSAGE_PATH + SETTINGS_SCREEN -> Screens.ProfileSettings")
                        Screens.ProfileSettings
                    }
                    // fixme Variant A : improved_enabling_location
                    else -> {
                        Timber.d("✅ Matched IN_APP_MESSAGE_PATH -> HomeFlow")
                        if (improvedEnablingLocation) Screens.HomeFlow else Screens.HomeFlowVariantA
                    }
                }
            else -> {
                Timber.w("❌ No match found for Deep Link")
                null
            }
        }

        Timber.d("🔗 Result: $result")
        return result
    }
}
