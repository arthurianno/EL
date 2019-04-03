package com.elta.android.domain.features.user.interactor

import com.elta.android.domain.features.user.model.Profile

const val DEFAULT_VALUE = 5.0
const val MIN = 2.0
const val MAX = 12.0
const val STEP = 0.1

fun increment(original: Double): Double = if (original < MAX) original.plus(STEP) else MAX
fun decrement(original: Double): Double = if (original > MIN) original.minus(STEP) else MIN

fun Profile.getHemoglobinLevel(): Double = this.hba1cLevel ?: DEFAULT_VALUE

fun Profile?.isHemoglobinLevelChanged(input: Double): Boolean = if (this?.hba1cLevel == null) true else hba1cLevel != input