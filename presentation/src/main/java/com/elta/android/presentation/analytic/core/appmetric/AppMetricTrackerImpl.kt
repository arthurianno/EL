package com.elta.android.presentation.analytic.core.appmetric

import com.elta.android.presentation.analytic.model.appmetric.AppMetricAttribute
import com.elta.android.presentation.analytic.model.appmetric.AppMetricEvent
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.profile.Attribute
import io.appmetrica.analytics.profile.UserProfile
import javax.inject.Inject

class AppMetricTrackerImpl @Inject constructor() : AppMetricTracker {

    override fun trackEvent(event: AppMetricEvent) {
        event.eventParams?.let {
            AppMetrica.reportEvent(event.eventName, mapOf(it))
        } ?: run {
            AppMetrica.reportEvent(event.eventName)
        }
        AppMetrica.sendEventsBuffer()
    }

    override fun setProfileAttributes(attributes: List<AppMetricAttribute>) {
        if (attributes.isNotEmpty()) {
            val profile = UserProfile
                .newBuilder()
                .apply {
                    attributes.forEach {
                        when (it) {
                            is AppMetricAttribute.Email ->
                                apply(Attribute.customString("e-mail").withValue(it.emailAddress))

                            is AppMetricAttribute.DiabetesType ->
                                apply(Attribute.customString("type_diabetes").withValue(it.type))

                            is AppMetricAttribute.Age ->
                                apply(Attribute.birthDate().withAge(it.years))

                            is AppMetricAttribute.Gender ->
                                apply(Attribute.gender().withValue(it.gender))
                        }
                    }
                }
                .build()

            AppMetrica.reportUserProfile(profile)
        }
    }
}
