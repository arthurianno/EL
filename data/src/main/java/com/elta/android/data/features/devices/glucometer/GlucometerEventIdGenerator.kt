package com.elta.android.data.features.devices.glucometer

interface GlucometerEventIdGenerator {

    fun generate(userId: String, glucometerId: String, dateToken: String): String
}
