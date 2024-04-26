package com.elta.android.presentation.features.profile.settings.emias.model

import com.elta.android.common.utils.CommonFormats.FORMAT_ONLY_DIGITS
import com.elta.android.presentation.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val DATE_LENGTH = 8
val OMS_RANGE = 9..16

fun String.isOmsValid() = this.validateOms() == null
fun String.isDateValid() = this.validateDate() == null

fun String.validateDate(): Int? {
    val date = parseToDate()

    return when {
        isEmpty() || isBlank() -> R.string.required_field_error
        length != DATE_LENGTH -> R.string.incorrect_length_error
        date == null -> R.string.incorrect_date_error
        date.isAgeValid() -> R.string.incorrect_age_error
        else -> null
    }
}

private fun String.parseToDate() = try {
    val formatter = SimpleDateFormat(
        FORMAT_ONLY_DIGITS,
        Locale.getDefault()
    )
    formatter.isLenient = false
    val parsedDate = formatter.parse(this)
    parsedDate

} catch (ex: Exception) {
    null
}

private fun Date.isAgeValid(): Boolean {
    val today = Date()
    return after(today)
}

fun String.validateOms(): Int? =
    when {
        isEmpty() || isBlank() -> R.string.required_field_error
        length !in OMS_RANGE -> R.string.incorrect_length_error
        else -> null
    }
