package com.elta.android.presentation.features.main.records.models

/**
 * Class to hold glucose ranges.
 * // TODO this class should be moved to domain layer, and support ranges set by user
 * // TODO values of ranges is for test only (need to improve)
 */
@Suppress("MagicNumber")
enum class GlucoseRange : ClosedRange<Double> {

    LOW {
        override val endInclusive = 3.99
        override val start = 0.0
    },

    MEDIUM {
        override val endInclusive = 7.99
        override val start = 4.0
    },

    HIGH {
        override val endInclusive = Double.MAX_VALUE
        override val start = 8.0
    }
}