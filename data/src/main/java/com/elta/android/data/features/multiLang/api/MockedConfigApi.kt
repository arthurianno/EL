package com.elta.android.data.features.multiLang.api

import com.elta.android.data.features.multiLang.models.ScreenConfigResponse
import io.reactivex.Single

class MockedConfigApi : ConfigApi {

    override fun getScreenConfigs(slugs: List<String>, langs: List<String>?): Single<List<ScreenConfigResponse>> {
        val mockConfigs = mutableListOf<ScreenConfigResponse>()
        slugs.forEachIndexed { index, slug ->
            if (index % 2 == 0) { // Возвращаем для четных индексов
                val translations = mutableMapOf<String, String>()
                translations["ru"] = when (slug) {
                    "connect_start" -> "Подключить" // Чистый текст для русского
                    else -> "Мок описание на русском"
                }
                translations["kk"] = when (slug) {
                    "connect_start" -> "Қосылу" // Чистый текст для казахского
                    else -> "Қазақ тіліндегі жалған сипаттама"
                }
                langs?.forEach { lang ->
                    if (lang != "ru" && lang != "kk") {
                        translations[lang] = "Mock description in $lang"
                    }
                }
                mockConfigs.add(ScreenConfigResponse(slug, translations, null)) // Без изображений
            }
        }
        if (slugs.size > 200) {
            return Single.error(IllegalArgumentException("limit-exceeded"))
        }
        return Single.just(mockConfigs)
    }
}