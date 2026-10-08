package com.elta.android.presentation.features.auth.login.viewmodel

import android.content.Context
import com.elta.android.common.logger.FirebaseStorage
import com.elta.android.domain.features.remoteconfig.interactor.GetFeatureConfigUseCase
import com.elta.android.domain.features.user.interactor.GetUserIdUseCase
import com.elta.android.domain.features.user.interactor.UpdateLanguageTagUseCase
import com.elta.android.domain.features.userinfo.interactor.GetProfileSettingsUseCase
import com.elta.android.domain.features.userinfo.interactor.GetUserInfoUseCase
import com.elta.android.presentation.utils.LocaleHelper
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.rx2.await

internal enum class LoginDestination { Activate, Onboarding, Home, HomeVariantA }

internal fun resolveLoginDestination(
    isActivated: Boolean,
    isEmailConfirmed: Boolean,
    isOnboarded: Boolean,
    improvedLocation: Boolean
): LoginDestination = when {
    !isActivated || !isEmailConfirmed -> LoginDestination.Activate
    !isOnboarded -> LoginDestination.Onboarding
    improvedLocation -> LoginDestination.Home
    else -> LoginDestination.HomeVariantA
}

class LoginPostAuth @Inject constructor(
    private val getUserId: GetUserIdUseCase,
    private val getUserInfo: GetUserInfoUseCase,
    private val getProfileSettings: GetProfileSettingsUseCase,
    private val updateLanguageTag: UpdateLanguageTagUseCase,
    private val getFeatureConfig: GetFeatureConfigUseCase,
    private val firebaseStorage: FirebaseStorage,
    private val context: Context
) {
    internal suspend fun complete(isActivated: Boolean): LoginDestination {
        if (!isActivated) return LoginDestination.Activate

        val userInfo = getUserInfo.execute().await()
        val profileSettings = getProfileSettings.execute().await()
        firebaseStorage.userLogin = getUserId.execute().await()
        try {
            updateLanguageTag.execute(
                UpdateLanguageTagUseCase.Params(LocaleHelper.getLanguage(context))
            ).await()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            if (error is Error) throw error
            // Login remains available when only the language update fails.
        }

        val confirmed = userInfo.isEmailConfirmed == true
        val onboarded = profileSettings.isOnboarded
        val improvedLocation = if (confirmed && onboarded) {
            getFeatureConfig().improvedEnablingLocation
        } else {
            false
        }
        return resolveLoginDestination(true, confirmed, onboarded, improvedLocation)
    }
}
