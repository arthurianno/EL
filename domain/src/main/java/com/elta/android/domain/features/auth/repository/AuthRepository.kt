package com.elta.android.domain.features.auth.repository

import io.reactivex.Completable
import io.reactivex.Single

interface AuthRepository {

    fun register(email: String, password: String): Completable

    fun login(email: String, password: String): Single<Boolean>

    fun checkEmail(): Single<Boolean>

    fun sendConfirmationLink(): Completable

    fun sendResetPasswordLink(email: String): Completable

    fun resetPassword(token: String, newPassword: String): Completable
}