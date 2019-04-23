@file:Suppress("MagicNumber")

package com.elta.android.domain.features.statistics.model

import com.elta.android.domain.features.diary.home.model.atEndOfDay
import com.nullgr.core.date.minusDay
import com.nullgr.core.date.withoutTime
import java.util.Date

sealed class Periods(override val start: Date, override val end: Date) : StatisticPeriod {

    class SevenDays : Periods(
        start = Date().minusDay(6).withoutTime(),
        end = Date().atEndOfDay()
    )

    class FourteenDays : Periods(
        start = Date().minusDay(13).withoutTime(),
        end = Date().atEndOfDay()
    )

    class ThirtyDays : Periods(
        start = Date().minusDay(29).withoutTime(),
        end = Date().atEndOfDay()
    )

    class NinetyDays : Periods(
        start = Date().minusDay(89).withoutTime(),
        end = Date().atEndOfDay()
    )
}