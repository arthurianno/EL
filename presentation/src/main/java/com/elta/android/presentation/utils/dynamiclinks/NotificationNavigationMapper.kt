package com.elta.android.presentation.utils.dynamiclinks

import android.net.Uri
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.navigation.support.SupportAppScreen

object NotificationNavigationMapper {

    fun notificationDataToScreen(uri: Uri): SupportAppScreen? {
        return when {
            uri.lastPathSegment.equals(HOME_SCREEN, true) -> Screens.HomeFlow
            else -> null
        }
    }
}
