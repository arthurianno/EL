@file:Suppress("SpreadOperator")

package com.elta.android.presentation.analytics

import com.elta.android.domain.features.user.model.Profile
import com.elta.android.presentation.analytics.model.AnalyticsEvent
import com.elta.android.presentation.analytics.model.AnalyticsEventParam
import com.elta.android.presentation.analytics.model.AnalyticsEventType
import com.elta.android.presentation.core.pm.BasePm
import java.math.BigInteger
import java.security.MessageDigest

private const val PERIOD_PARAM_PATTERN = "%d_days"

fun encodeUserId(id: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val hash = digest.digest(id.toByteArray(Charsets.UTF_8))
    return String.format("%064x", BigInteger(1, hash))
}

fun getPeriodParam(count: Int) = String.format(PERIOD_PARAM_PATTERN, count)

fun BasePm.updateStableParam(id: String? = null, profile: Profile? = null) {
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