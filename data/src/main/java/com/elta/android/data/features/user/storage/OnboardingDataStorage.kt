package com.elta.android.data.features.user.storage

import android.content.SharedPreferences
import com.elta.android.data.features.common.storage.UserHolder
import com.nullgr.core.preferences.get
import com.nullgr.core.preferences.set
import javax.inject.Inject

class OnboardingDataStorage @Inject constructor(
    private val pref: SharedPreferences,
    private val userHolder: UserHolder
) : OnboardingStorage {

    override var isOnboardingPassed: Boolean
        get() = pref["$IS_ONBOARDING_PASSED${userHolder.currentUser}"] ?: false
        set(value) {
            pref["$IS_ONBOARDING_PASSED${userHolder.currentUser}"] = value
        }

    private companion object {
        const val IS_ONBOARDING_PASSED = "is_onboarding_passed"
    }
}