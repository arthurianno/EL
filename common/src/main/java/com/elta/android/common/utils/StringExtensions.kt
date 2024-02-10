package com.elta.android.common.utils

import kotlin.text.StringBuilder

fun String.hideMac(): String {
    val parts = this.split(':')
    if (parts.size < 6) return this
    val hiddenItems = intArrayOf(2,3,4)
    val string = StringBuilder()
    parts.forEachIndexed { index, part ->
        val value = if (index in hiddenItems) "**" else part
        string.append(value)
        if (index < parts.size - 1) string.append(":")
    }
    return string.toString()
}

fun String.hideEmail(): String {
    val atIndex = indexOf('@')
    if (atIndex == -1 || atIndex == 0 || atIndex == length - 1) {
        return "Invalid email format"
    }

    val username = substring(0, atIndex)
    val hiddenCharsCount = minOf(maxOf(username.length / 2, 1), 3)  // Максимум 3 скрытых символа
    val hiddenChars = "*".repeat(hiddenCharsCount)
    val visibleChars = username.substring(0, username.length - hiddenCharsCount)

    return "$visibleChars$hiddenChars@${substring(atIndex + 1)}.${hashCode()}"
}