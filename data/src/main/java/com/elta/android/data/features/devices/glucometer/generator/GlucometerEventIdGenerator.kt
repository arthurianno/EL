package com.elta.android.data.features.devices.glucometer.generator

interface GlucometerEventIdGenerator {

    fun generate(userId: String, glucometerId: String, dateToken: String): String
}
