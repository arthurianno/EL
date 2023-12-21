package com.elta.android.domain.features.statistics.interactor

import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.domain.features.diary.home.model.CalculatorFlow
import com.elta.android.domain.features.diary.medicines.model.InsulinMedicamentStatistic
import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings
import com.elta.android.domain.features.statistics.model.daily.DailyBreadStatisticModel
import com.elta.android.domain.features.statistics.model.daily.DailyInsulinStatisticModel
import com.elta.android.domain.features.statistics.model.daily.DailyStatisticModel
import com.elta.android.domain.features.user.model.GlucoseFormat
import org.threeten.bp.LocalDate

fun buildDailyStatisticModel(
    date: LocalDate,
    eventsPerDay: Map<EventType, List<EventV2>>,
    settings: GlucoseLevelSettings,
    glucoseFormat: GlucoseFormat,
    insulinMedicamentStatistic: InsulinMedicamentStatistic,
    calculatorFlow: CalculatorFlow
): DailyStatisticModel {
    return DailyStatisticModel(
        date = date,
        glucose = buildGlucoseStatisticModel(
            glucoseEventsPerPeriod = eventsPerDay[EventType.Glucose],
            settings = settings,
            glucoseFormat = glucoseFormat,
            forPeriod = false
        ),
        insulin = buildDailyInsulinStatisticModel(eventsPerDay[EventType.Insulin], insulinMedicamentStatistic),
        bread = buildDailyBreadStatisticModel(eventsPerDay[EventType.Bread(calculatorFlow)]),
        activity = buildActivityStatisticModel(eventsPerDay[EventType.Activity])
    )
}

fun buildDailyInsulinStatisticModel(
    insulinEventsPerDay: List<EventV2>?,
    insulinMedicamentStatistic: InsulinMedicamentStatistic,
): DailyInsulinStatisticModel {
    var totalBolusLevel = 0.0
    var totalBasalLevel = 0.0
    var totalLevel = 0.0

    insulinEventsPerDay?.forEach { event ->
        val value = event.value
        if (value != null && value != 0.0) {
            if (event.isBolusInsulin(insulinMedicamentStatistic)) {
                totalBolusLevel += value
            }

            if (event.isBasalInsulin(insulinMedicamentStatistic)) {
                totalBasalLevel += value
            }

            if (event.isBasalOrBolus(insulinMedicamentStatistic)) {
                totalLevel += value
            }
        }
    }

    return DailyInsulinStatisticModel(
        totalBolusLevel = totalBolusLevel,
        totalBasalLevel = totalBasalLevel,
        totalLevel = totalLevel
    )
}

fun buildDailyBreadStatisticModel(breadEventsPerDay: List<EventV2>?): DailyBreadStatisticModel {
    var totalLevel = 0.0

    breadEventsPerDay?.forEach { event ->
        val value = event.value
        if (value != null && value != 0.0) {
            totalLevel += value
        }
    }

    return DailyBreadStatisticModel(
        totalLevel = totalLevel
    )
}
