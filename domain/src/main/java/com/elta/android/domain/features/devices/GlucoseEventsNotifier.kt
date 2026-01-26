package com.elta.android.domain.features.devices

/**
 * Интерфейс для уведомлений об изменениях событий глюкозы.
 * Используется для связи между data и presentation слоями без нарушения Clean Architecture.
 */
interface GlucoseEventsNotifier {
    /**
     * Уведомляет об изменении событий глюкозы (добавление, обновление, удаление)
     */
    fun notifyEventsChanged()
}

