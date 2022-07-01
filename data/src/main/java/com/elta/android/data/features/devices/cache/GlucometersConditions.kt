package com.elta.android.data.features.devices.cache

import com.elta.android.data.features.common.cache.Condition

sealed class GlucometersConditions : Condition {

    object Primary : GlucometersConditions()
}
