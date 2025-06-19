package com.elta.android.data.features.auth.model

import com.google.gson.annotations.SerializedName

/**
 * @param email Электронная почта.
 * @param password Пароль от аккаунта.
 * @param activateAccount Указывает на необходимость восстановить удалённый аккаунт.
 */
data class AuthNetworkRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("activateAccount") val activateAccount: Boolean
)
