package com.elta.android.presentation.utils

private const val PIN_MUL_COEFFICIENT = 599681139
private const val PIN_PLUS_COEFFICIENT = 123
private const val PIN_AND_COEFFICIENT = 0xFFFFFFFF
private const val PIN_DIV_COEFFICIENT = 1000
private const val PIN_LENGTH = 3
private const val PIN_ZERO_NUMBER = "0"
private const val NUMBERS_COUNT_FOR_PIN = 6

@Throws(Exception::class)
internal fun String.extractPinCode(): String =
    this.drop(1)
        .takeLast(NUMBERS_COUNT_FOR_PIN)
        .toLong()
        .times(PIN_MUL_COEFFICIENT)
        .plus(PIN_PLUS_COEFFICIENT)
        .and(PIN_AND_COEFFICIENT)
        .mod(PIN_DIV_COEFFICIENT)
        .toString()
        .run {
            "${PIN_ZERO_NUMBER.repeat(PIN_LENGTH - this.length)}$this"
        }
