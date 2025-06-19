package com.elta.android.domain.features.auth.repository

import io.reactivex.Completable
import io.reactivex.Single

@Suppress("TooManyFunctions", "ComplexInterface")
interface AuthRepository {

    fun register(email: String, password: String): Completable

    /**
     * Авторизация аккаунта и восстановление.
     * @param email Электронная почта.
     * @param password Пароль от аккаунта.
     * @param activateAccount Указывает на необходимость восстановления аккаунта. По умолчанию false.
     */
    fun login(email: String, password: String, activateAccount: Boolean = false): Single<Boolean>

    fun isEmailConfirmed(): Single<Boolean>

    fun sendConfirmationLink(): Completable

    fun sendResetPasswordLink(email: String): Completable

    fun resetPassword(token: String, newPassword: String): Completable

    fun changePassword(currentPassword: String, newPassword: String): Completable

    fun checkTokenOwner(token: String): Single<Boolean>

    fun confirmEmail(token: String): Completable

    fun logout(): Completable

    fun deleteAccount(): Completable
}
