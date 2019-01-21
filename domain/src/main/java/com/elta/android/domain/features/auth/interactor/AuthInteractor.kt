package com.elta.android.domain.features.auth.interactor

import android.util.Patterns
import java.util.regex.Pattern

val emailPattern: Pattern = Patterns.EMAIL_ADDRESS
val passwordPattern: Pattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#\$%^&+=._])(?=\\S+\$).{8,}\$")

fun isEmailValid(email: String): Boolean = emailPattern.matcher(email).matches()
fun isPasswordValid(password: String): Boolean = passwordPattern.matcher(password).matches()