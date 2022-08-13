package com.elta.android.domain.features.auth.interactor

import java.util.regex.Pattern

val emailPattern: Pattern =
    Pattern.compile(
        "^([a-z0-9_.+-]{1,64}+)@([a-z]+)\\.([a-z]{2,7})$",
        Pattern.CASE_INSENSITIVE
    )

@Suppress("MaxLineLength")
val passwordPattern: Pattern =
    Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+\$).{8,}\$")

fun isEmailValid(email: String): Boolean = emailPattern.matcher(email).matches()
fun isPasswordValid(password: String): Boolean = passwordPattern.matcher(password).matches()
