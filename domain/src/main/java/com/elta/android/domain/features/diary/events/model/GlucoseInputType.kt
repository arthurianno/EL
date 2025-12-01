package com.elta.android.domain.features.diary.events.model

enum class GlucoseInputType {
    MANUAL,    // Введено вручную пользователем
    AUTO,      // Автоматически с глюкометра по Bluetooth
    GOOGLE_FIT // Синхронизировано из Google Fit / Health Connect
}
