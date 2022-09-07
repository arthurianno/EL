package com.elta.android.presentation.utils

import android.text.InputFilter
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import androidx.appcompat.widget.AppCompatEditText
import io.reactivex.functions.Consumer

fun EditText.toggleSecure(): Boolean {
    transformationMethod = when (transformationMethod == null) {
        true -> PasswordTransformationMethod.getInstance()
        else -> null
    }
    setSelection(text.length)
    return isSecure()
}

fun EditText.isSecure(): Boolean = transformationMethod != null

fun <E : AppCompatEditText> E.error(): Consumer<String> = Consumer {
    error = when (it.isEmpty()) {
        true -> null
        else -> it
    }
}

fun AppCompatEditText.setEmojiFilter() {
    val emojiFilter = InputFilter { source, start, end, dest, dstart, dend ->
        source.forEach {
            if (it.isSurrogate()) return@InputFilter ""
        }
        return@InputFilter null
    }
    filters += emojiFilter
}
