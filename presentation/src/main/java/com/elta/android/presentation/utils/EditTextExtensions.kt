package com.elta.android.presentation.utils

import android.support.v7.widget.AppCompatEditText
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import com.rengwuxian.materialedittext.MaterialEditText
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

fun MaterialEditText.clearError() {
    error = null
}