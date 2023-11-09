package com.elta.android.presentation.utils

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.elta.android.presentation.features.profile.settings.dialogs.glucose.model.COMMA_CHAR
import com.elta.android.presentation.features.profile.settings.dialogs.glucose.model.DOT_CHAR

const val PORTION_VALUE_REGEX = "^(\\d{1,4})(?:[.|,]\\d{0,1})?"
const val PORTION_INTEGER_LENGTH_REGEX = "\\d{5}"
const val PORTION_COUNT_INTEGER_PART = 4

const val BREAD_UNIT_VALUE_REGEX = "^(\\d{1,2})(?:[.|,]\\d{0,1})?"
const val BREAD_UNIT_INTEGER_LENGTH_REGEX = "\\d{3}"
const val BREAD_UNIT_COUNT_INTEGER_PART = 2

const val GLUCOSE_VALUE_REGEX = "^([1-9])([0-9]?)([.|,]\\d?)?"
const val GLUCOSE_INTEGER_LENGTH_REGEX = "\\d{3}"
const val GLUCOSE_COUNT_INTEGER_PART = 2

fun createTextFilterForDoubleValue(
    regexInteger: String,
    regexMain: String,
    countIntegerPart: Int
) = { textFieldValue: TextFieldValue ->
    with(textFieldValue) {
        var selection = textFieldValue.selection
        val textField = text
            .replace(DOT_CHAR, COMMA_CHAR)
            .replace(regexInteger.toRegex()) {
                val newText = it.value.take(countIntegerPart) + COMMA_CHAR + it.value.last()
                selection = TextRange(newText.length)
                newText
            }
            .takeIf { it.matches(regexMain.toRegex()) || it.isEmpty() }

        textField?.let {
            copy(
                text = it,
                selection = selection
            )
        }
    }
}

