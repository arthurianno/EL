package com.elta.android.domain.features.devices.interactor

import com.elta.android.domain.features.devices.model.GlucometerEvent

/** A clock mismatch makes the timestamp untrusted, but it is still the device's timestamp. */
fun List<GlucometerEvent>.markInvalidTimeForOutOfSyncClock(
    isTimeOutOfSync: Boolean
): List<GlucometerEvent> =
    if (isTimeOutOfSync) map { it.copy(isTimeInvalid = true) } else this
