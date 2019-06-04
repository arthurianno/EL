@file:Suppress("MagicNumber")

package com.elta.android.domain.features.statistics.model

import com.elta.android.common.utils.atEndOfDay
import com.elta.android.common.utils.atStartOfDay
import org.threeten.bp.LocalDateTime

sealed class Periods(override val start: LocalDateTime, override val end: LocalDateTime) : StatisticPeriod {

    class SevenDays : Periods(
        start = LocalDateTime.now().minusDays(6).atStartOfDay(),
        end = LocalDateTime.now().atEndOfDay()
    )

    class FourteenDays : Periods(
        start = LocalDateTime.now().minusDays(13).atStartOfDay(),
        end =LocalDateTime.now().atEndOfDay()
    )

    class ThirtyDays : Periods(
        start = LocalDateTime.now().minusDays(29).atStartOfDay(),
        end = LocalDateTime.now().atEndOfDay()
    )

    class NinetyDays : Periods(
        start = LocalDateTime.now().minusDays(89).atStartOfDay(),
        end = LocalDateTime.now().atEndOfDay()
    )
}