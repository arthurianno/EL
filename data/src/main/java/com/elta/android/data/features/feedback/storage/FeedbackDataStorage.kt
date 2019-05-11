package com.elta.android.data.features.feedback.storage

import android.content.SharedPreferences
import com.elta.android.data.features.common.storage.UserHolder
import com.nullgr.core.preferences.get
import com.nullgr.core.preferences.set
import javax.inject.Inject

class FeedbackDataStorage @Inject constructor(
    private val userHolder: UserHolder,
    private val pref: SharedPreferences
) : FeedbackStorage {

    override var isFeedbackWasSent: Boolean
        get() = pref[userHolder.currentUser.toString()] ?: false
        set(value) {
            pref[userHolder.currentUser.toString()] = value
        }
}