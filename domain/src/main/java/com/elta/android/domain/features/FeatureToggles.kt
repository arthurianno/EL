package com.elta.android.domain.features

object FeatureToggles {
    const val isEnableIiotSdkFeature = true
    const val isEnableConsultantFeature = false

    /**
     * Включаем этот флаг для сборок которые будет распространяться не через Google Play.
     * Например устройства Huawei, которые не поддерживают google-сервисы и распространяются вручную заказчиком.
     **/
    const val isEnableForUntrackedBuild = false
}
