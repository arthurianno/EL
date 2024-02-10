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