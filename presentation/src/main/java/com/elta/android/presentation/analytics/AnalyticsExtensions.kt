@file:Suppress("SpreadOperator")

package com.elta.android.presentation.analytics

import android.util.Base64
import com.elta.android.domain.features.user.model.Profile
import com.elta.android.presentation.analytics.model.AnalyticsEvent
import com.elta.android.presentation.analytics.model.AnalyticsEventParam
import com.elta.android.presentation.analytics.model.AnalyticsEventType
import com.elta.android.presentation.core.pm.BasePm

private const val PERIOD_PARAM_PATTERN = "%d_days"

private fun encodeUserId(id: Long): String {
    val emailCharArray = id.toString().toCharArray()

    val lastChar = emailCharArray.last()
    val preLastIndex = emailCharArray.size - 2
    emailCharArray[emailCharArray.lastIndex] = emailCharArray[preLastIndex]
    emailCharArray[preLastIndex] = lastChar
    val swappedEmailHashCodeByteArray = String(emailCharArray).toByteArray()

    return String(Base64.encode(swappedEmailHashCodeByteArray, Base64.DEFAULT))
}

fun getPeriodParam(count: Int) = String.format(PERIOD_PARAM_PATTERN, count)

fun BasePm.updateStableParam(id: Long? = null, profile: Profile? = null) {
    val params = hashMapOf<String, String>()
    id?.let { params[AnalyticsEventParam.ACCOUNT] = encodeUserId(id) }
    params[AnalyticsEventParam.GENDER] = profile?.gender?.name ?: false.toString()
    params[AnalyticsEventParam.DIABETES] = profile?.diabetes?.name ?: false.toString()
    analytics.updateStableParams(params)
}

fun BasePm.trackEvent(event: AnalyticsEvent?) = event?.let { analytics.trackEvent(event) }

fun BasePm.trackEvent(
    @AnalyticsEventType name: String,
    vararg pairs: Pair<String, String>
) =
    analytics.trackEvent(AnalyticsEvent(name, hashMapOf(*pairs)))