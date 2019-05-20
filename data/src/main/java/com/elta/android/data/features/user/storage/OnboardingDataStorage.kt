package com.elta.android.data.features.user.storage

import android.content.SharedPreferences
import com.nullgr.core.preferences.get
import com.nullgr.core.preferences.set
import javax.inject.Inject

class OnboardingDataStorage @Inject constructor(
    private val pref: SharedPreferences
) : OnboardingStorage {

    override var isOnboardingPassed: Boolean
        get() = pref[IS_ONBOARDING_PASSED] ?: false
        set(value) {
            pref[IS_ONBOARDING_PASSED] = value
        }

    private companion object {
        const val IS_ONBOARDING_PASSED = "is_onboarding_passed"
    }
}