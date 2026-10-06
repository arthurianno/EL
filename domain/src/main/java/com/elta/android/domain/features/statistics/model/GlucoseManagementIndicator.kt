package com.elta.android.domain.features.statistics.model

/** GMI (%) from mean glucose in mmol/L (Bergenstal et al., Diabetes Care 2018). */
fun glucoseManagementIndicatorPercent(meanGlucoseMmolPerLiter: Double): Double =
    3.31 + 0.431 * meanGlucoseMmolPerLiter
