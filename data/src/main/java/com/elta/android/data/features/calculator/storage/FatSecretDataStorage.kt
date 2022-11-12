package com.elta.android.data.features.calculator.storage

import com.nullgr.core.security.prefs.CryptoPreferences
import javax.inject.Inject

private const val FATSECRET_TOKEN = "FatSecret_token"

class FatSecretDataStorage @Inject constructor(
    private val preferences: CryptoPreferences
) : FatSecretStorage {

    override var token: String?
        get() = preferences.getString(FATSECRET_TOKEN, null)
        set(value) {
            preferences.setString(FATSECRET_TOKEN, value)
        }
}
