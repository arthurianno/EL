package com.elta.android.domain.features.user.interactor

import com.elta.android.domain.features.user.model.Profile
import java.math.BigDecimal

const val DEFAULT_VALUE = 5.0
const val MIN = 2.0
const val MAX = 20.0
const val STEP = 0.1

fun increment(original: Double): Double = (if (original < MAX) original.plus(STEP) else MAX).round(1)
fun decrement(original: Double): Double = (if (original > MIN) original.minus(STEP) else MIN).round(1)

fun Profile.getHemoglobinLevel(): Double = this.hba1cLevel ?: DEFAULT_VALUE

fun Double.round(places: Int): Double = BigDecimal(this).setScale(places, BigDecimal.ROUND_HALF_EVEN).toDouble()