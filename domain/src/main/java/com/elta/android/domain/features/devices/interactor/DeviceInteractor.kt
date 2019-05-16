package com.elta.android.domain.features.devices.interactor

private val pinRegex = Regex("^[0-9]{3}")

fun isPinValid(pin: String) = pin.matches(pinRegex)
