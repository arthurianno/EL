package com.elta.android.data.features.feedback.storage

import android.content.SharedPreferences
import com.elta.android.data.features.common.storage.UserHolder
import com.nullgr.core.preferences.get
import com.nullgr.core.preferences.set
import javax.inject.Inject

class FeedbackDataStorage @Inject constructor(
    private val pref: SharedPreferences,
    private val userHolder: UserHolder
) : FeedbackStorage {

    override var isFeedbackWasSent: Boolean
        get() = pref["$IS_FEEDBACK_SENT${userHolder.currentUser}"] ?: false
        set(value) {
            pref["$IS_FEEDBACK_SENT${userHolder.currentUser}"] = value
        }

    private companion object {
        const val IS_FEEDBACK_SENT = "is_feedback_sent"
    }
}