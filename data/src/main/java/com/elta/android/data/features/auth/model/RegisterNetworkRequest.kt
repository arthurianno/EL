package com.elta.android.data.features.auth.model

import com.google.gson.annotations.SerializedName

/**
 * Тело запроса POST /auth/v1/accounts.
 *
 * @param email электронная почта
 * @param password пароль
 * @param activateAccount восстановление удалённого аккаунта
 * @param languageTag язык интерфейса (SupportedLanguageTag: ru/en)
 * @param countryCode alpha-2 код страны, по умолчанию "RU"
 */
data class RegisterNetworkRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("activateAccount") val activateAccount: Boolean,
    @SerializedName("languageTag") val languageTag: String? = null,
    @SerializedName("countryCode") val countryCode: String? = null
)

