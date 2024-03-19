package com.elta.android.data.features.version.model

import com.google.gson.annotations.SerializedName

/**
 * Класс для отправки версии МП в админ-панель сервера
 * @param appId статический идентификатор платформы, задаётся в репозитории. Нужен для разделяния платформ на беке.
 * @param appVersion версия приложения. Задаётся из CI конфигурации. При локальной сборке будет установлено стандартное значение.
 * При сборке в гитлабе через теги автоматически выставляется из названия тега
 **/
data class AppVersionNetworkRequest(
    @SerializedName("appId") val appId: String,
    @SerializedName("appVersion") val appVersion: String
)
