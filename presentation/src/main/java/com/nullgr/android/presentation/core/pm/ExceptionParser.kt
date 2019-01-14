package com.nullgr.android.presentation.core.pm

interface ExceptionParser {

    fun parse(e: Exception): String
}