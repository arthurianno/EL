package com.elta.android.data.features.auth.storage

import android.content.SharedPreferences
import com.elta.android.data.features.common.storage.UserHolder
import com.nullgr.core.preferences.get
import com.nullgr.core.preferences.set
import javax.inject.Inject

class EmailDataStorage @Inject constructor(
    private val pref: SharedPreferences,
    private val userHolder: UserHolder
) : EmailStorage {

    override var isEmailConfirmed: Boolean
        get() = pref["$IS_EMAIL_CONFMED${userHolder.currentUser}"] ?: false
        set(value) {
            pref["$IS_EMAIL_CONFMED${userHolder.currentUser}"] = value
        }

    private companion object {
        const val IS_EMAIL_CONFMED = "is_email_confirmed"
    }
}