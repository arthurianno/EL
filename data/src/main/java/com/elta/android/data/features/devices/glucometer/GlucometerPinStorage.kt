package com.elta.android.data.features.devices.glucometer

interface GlucometerPinStorage {

    fun getPin(address: String): String?

    fun setPin(address: String, pinCode: String)
}