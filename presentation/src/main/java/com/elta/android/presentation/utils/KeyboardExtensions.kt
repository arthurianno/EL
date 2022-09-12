package com.elta.android.presentation.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import io.reactivex.functions.Consumer

fun View.keyboardVisibility(): Consumer<in Boolean> {
    return Consumer {
        when (it) {
            true -> showKeyboardFun()
            else -> hideKeyboardFun()
        }
    }
}

fun View.showKeyboardFun() {
    val function = {
        if (requestFocus()) {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(this, 0)
        }
    }

    function.invoke()
    post {
        function.invoke()
    }
}

fun View.hideKeyboardFun() {
    val function = {
        clearFocus()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    function.invoke()
    post {
        function.invoke()
    }
}
