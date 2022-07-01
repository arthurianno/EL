package com.elta.android.presentation.core.pm

interface ExceptionParser {

    fun parse(e: Exception): String
}
