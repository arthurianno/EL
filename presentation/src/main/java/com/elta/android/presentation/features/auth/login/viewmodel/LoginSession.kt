package com.elta.android.presentation.features.auth.login.viewmodel

import android.content.Context
import com.elta.android.common.logger.FirebaseStorage
import com.elta.android.domain.features.auth.interactor.LoginUseCase
import com.elta.android.domain.features.auth.interactor.RestoreProfileUseCase
import com.elta.android.domain.features.remoteconfig.interactor.GetFeatureConfigUseCase
import com.elta.android.domain.features.user.interactor.GetUserIdUseCase
import com.elta.android.domain.features.user.interactor.UpdateLanguageTagUseCase
import com.elta.android.domain.features.userinfo.interactor.GetProfileSettingsUseCase
import com.elta.android.domain.features.userinfo.interactor.GetUserInfoUseCase
import com.elta.android.presentation.analytic.core.analytics.Analytics
import com.elta.android.presentation.analytic.encodeUserId
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEvent
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEventParam
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEventType
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

class LoginSession @Inject constructor(
    private val login: LoginUseCase,
    private val restoreProfile: RestoreProfileUseCase,
    private val getUserId: GetUserIdUseCase,
    private val getUserInfo: GetUserInfoUseCase,
    private val getProfileSettings: GetProfileSettingsUseCase,
    private val updateLanguageTag: UpdateLanguageTagUseCase,
    private val getFeatureConfig: GetFeatureConfigUseCase,
    private val firebaseStorage: FirebaseStorage,
    private val analytics: Analytics,
    private val context: Context
) {
    suspend fun authenticate(email: String, password: String): Boolean {
        val isActivated = login.execute(LoginUseCase.Params(email, password)).await()
        analytics.trackEvent(AnalyticsEvent(AnalyticsEventType.LOG_IN))
        if (isActivated) {
            val id = getUserId.execute().await()
            analytics.updateStableParams(
                mapOf(
                    AnalyticsEventParam.ACCOUNT to encodeUserId(id),
                    AnalyticsEventParam.GENDER to false.toString(),
                    AnalyticsEventParam.DIABETES to false.toString()
                )
            )
        }
        return isActivated
    }

    suspend fun restore(email: String, password: String): Boolean =
        restoreProfile.execute(RestoreProfileUseCase.Params(email, password)).await()

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

        val improvedLocation = if (userInfo.isEmailConfirmed == true && profileSettings.isOnboarded) {
            getFeatureConfig().improvedEnablingLocation
        } else {
            false
        }
        return resolveLoginDestination(
            isActivated = true,
            isEmailConfirmed = userInfo.isEmailConfirmed == true,
            isOnboarded = profileSettings.isOnboarded,
            improvedLocation = improvedLocation
        )
    }
}
