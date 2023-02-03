package com.elta.android.domain.features.user

private const val MAX_NAME_LENGTH = 50
private const val MIN_NAME_LENGTH = 2
private const val REGEX_NAME_PATTERN = "^(?:[A-Za-zА-Яа-я '(),\\-.])*\$"

fun String.isNameValid() = !(this.isTooShort() || this.isTooLong() || this.hasWrongChars())

fun String.isTooLong() = this.length > MAX_NAME_LENGTH

fun String.isTooShort() = this.length < MIN_NAME_LENGTH

fun String.hasWrongChars() = !this.contains(regex = Regex(REGEX_NAME_PATTERN))
