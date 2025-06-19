package com.elta.android.presentation.utils.dynamiclinks

import android.net.Uri
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.navigation.support.SupportAppScreen

object NotificationNavigationMapper {

    fun notificationDataToScreen(uri: Uri, improvedEnablingLocation: Boolean): SupportAppScreen? {
        return when {
            // fixme Variant A : improved_enabling_location
            uri.lastPathSegment.equals(HOME_SCREEN, true) -> if (improvedEnablingLocation) Screens.HomeFlow else Screens.HomeFlowVariantA
            else -> null
        }
    }
}
