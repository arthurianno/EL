package com.elta.android.data.features.devices.glucometer

import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultGlucometerEventIdGenerator @Inject constructor() : GlucometerEventIdGenerator {

    override fun generate(glucometerId: String, dateToken: String): String =
        UUID.nameUUIDFromBytes("$glucometerId$dateToken".toByteArray()).toString()
}