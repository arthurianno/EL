package com.elta.android.domain.features.devices.model

data class Glucometer(
    val id: String,
    val address: String,
    val name: String?,
    val isPrimary: Boolean
)