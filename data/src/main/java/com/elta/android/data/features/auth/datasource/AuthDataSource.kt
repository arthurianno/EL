package com.elta.android.data.features.auth.datasource

import com.elta.android.data.features.auth.model.EmailStatusNetworkResponse
import com.elta.android.data.features.auth.model.LoginNetworkResponse
import com.elta.android.data.features.auth.model.TokenOwnerNetworkResponse
import com.elta.android.data.features.auth.model.TokensNetworkResponse
import io.reactivex.Completable
import io.reactivex.Single

interface AuthDataSource {

    fun register(email: String, password: String): Single<TokensNetworkResponse>

    /**
    * Авторизация аккаунта и восстановление.
     * @param email Электронная почта.
     * @param password Пароль от аккаунта.
     * @param activateAccount Указывает на необходимость восстановления аккаунта.
     */
    fun login(email: String, password: String, activateAccount: Boolean): Single<LoginNetworkResponse>

    fun sendConfirmationLink(): Completable

    fun isEmailConfirmed(): Single<EmailStatusNetworkResponse>

    fun sendResetPasswordLink(email: String): Completable

    fun resetPassword(token: String, newPassword: String): Completable

    fun changePassword(currentPassword: String, newPassword: String): Completable

    fun checkTokenOwner(token: String): Single<TokenOwnerNetworkResponse>

    fun confirmEmail(token: String): Completable

    fun deleteAccount(): Completable
}
