package com.elta.android.domain.features.devices.repository

interface PinRepository {

    fun getPin(address: String): String?

    fun savePin(address: String, pin: String)

    fun clearPin(address: String)

}
