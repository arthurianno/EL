package com.elta.android.data.features.devices.glucometer

interface GlucometerEventIdGenerator {

    fun generate(glucometerId: String, dateToken: String): String
}