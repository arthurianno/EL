package com.elta.android.presentation.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

object NumberFormatter {

    val numberFormat by lazy {
        DecimalFormat("#.#").apply {
            minimumFractionDigits = 1
            decimalFormatSymbols = DecimalFormatSymbols().apply {
                decimalSeparator = ','
            }
        }
    }

    fun format(value: Double): String = numberFormat.format(value)
}