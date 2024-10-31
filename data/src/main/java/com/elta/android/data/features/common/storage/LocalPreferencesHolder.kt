package com.elta.android.data.features.common.storage

import android.content.SharedPreferences
import com.elta.android.domain.features.appsettings.model.BackendVariant
import com.nullgr.core.preferences.set
import javax.inject.Inject

class LocalPreferencesHolder @Inject constructor(
    private val preferences: SharedPreferences
) : PreferencesHolder {
    override var backendVariant: String?
        get() = preferences.getString(BackendVariant.NAME_BACKEND_VARIANT, "")
        set(value) {
            preferences[BackendVariant.NAME_BACKEND_VARIANT] = value
        }


    override var manualGlucoseRemind: Boolean
        // Если нет значения, то возвращаем true, чтобы отобразить диалог при создании события Ручной замер глюкозы
        get() = preferences.getBoolean(MANUAL_GLUCOSE_REMIND, true)
        set(value) {
            preferences[MANUAL_GLUCOSE_REMIND] = value
        }

    companion object {
        const val MANUAL_GLUCOSE_REMIND = "manual_glucose_remind"

    }
}
