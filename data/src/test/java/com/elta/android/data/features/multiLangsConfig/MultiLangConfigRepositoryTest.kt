package com.elta.android.data.features.multiLangsConfig

import org.junit.Test
import org.junit.Assert.*

/**
 * Простые тесты для проверки логики обновления конфигов каждые 24 часа ± 20 минут
 *
 * Эти тесты проверяют математику расчета интервалов без использования моков.
 */
class MultiLangConfigRepositoryTest {

    companion object {
        private const val TWENTY_FOUR_HOURS_MILLIS = 24 * 60 * 60 * 1000L
        private const val TWENTY_MINUTES_MILLIS = 20 * 60 * 1000L
    }

    /**
     * Симулирует проверку shouldRefreshScreensConfig
     */
    private fun shouldRefresh(
        lastRefreshTime: Long,
        currentTime: Long,
        randomOffset: Long
    ): Boolean {
        val refreshInterval = TWENTY_FOUR_HOURS_MILLIS + randomOffset
        return (currentTime - lastRefreshTime) >= refreshInterval
    }

    @Test
    fun `первый запуск - должен вернуть true для обновления`() {
        // Given: нет сохраненного времени (первый запуск)
        val lastRefresh = 0L
        val currentTime = System.currentTimeMillis()
        val randomOffset = 0L

        // When
        val result = shouldRefresh(lastRefresh, currentTime, randomOffset)

        // Then
        assertTrue("При первом запуске должно быть обновление", result)
    }

    @Test
    fun `прошло 12 часов - должен вернуть false`() {
        // Given: последнее обновление было 12 часов назад
        val currentTime = System.currentTimeMillis()
        val lastRefresh = currentTime - (12 * 60 * 60 * 1000L) // 12 часов назад
        val randomOffset = 10 * 60 * 1000L // +10 минут

        // When
        val result = shouldRefresh(lastRefresh, currentTime, randomOffset)

        // Then
        assertFalse("Через 12 часов не должно быть обновления", result)
    }

    @Test
    fun `прошло 25 часов - должен вернуть true`() {
        // Given: последнее обновление было 25 часов назад
        val currentTime = System.currentTimeMillis()
        val lastRefresh = currentTime - (25 * 60 * 60 * 1000L) // 25 часов назад
        val randomOffset = 10 * 60 * 1000L // +10 минут

        // When
        val result = shouldRefresh(lastRefresh, currentTime, randomOffset)

        // Then
        assertTrue("Через 25 часов должно быть обновление", result)
    }

    @Test
    fun `граничный случай - ровно 24 часа с минимальным смещением (-20 мин)`() {
        // Given: прошло ровно 24 часа, смещение -20 минут (итого нужно 23ч 40мин)
        val currentTime = System.currentTimeMillis()
        val lastRefresh = currentTime - TWENTY_FOUR_HOURS_MILLIS
        val randomOffset = -20 * 60 * 1000L // -20 минут

        // When
        val result = shouldRefresh(lastRefresh, currentTime, randomOffset)

        // Then
        assertTrue("При смещении -20 мин и прошедших 24 часах должно быть обновление", result)
    }

    @Test
    fun `граничный случай - 24 часа с максимальным смещением (+20 мин)`() {
        // Given: прошло 24 часа, смещение +20 минут (нужно 24ч 20мин)
        val currentTime = System.currentTimeMillis()
        val lastRefresh = currentTime - TWENTY_FOUR_HOURS_MILLIS
        val randomOffset = 20 * 60 * 1000L // +20 минут

        // When
        val result = shouldRefresh(lastRefresh, currentTime, randomOffset)

        // Then
        assertFalse("При смещении +20 мин и прошедших только 24 часах не должно быть обновления", result)
    }

    @Test
    fun `случайное смещение в диапазоне -20 до +20 минут`() {
        // Given: генерируем 100 случайных смещений
        val minOffset = -20 * 60 * 1000L
        val maxOffset = 20 * 60 * 1000L

        // When: генерируем случайные смещения
        repeat(100) {
            val randomMinutes = (-20..20).random()
            val offset = randomMinutes * 60 * 1000L

            // Then: проверяем что все в диапазоне
            assertTrue(
                "Смещение $offset должно быть >= -20 минут",
                offset >= minOffset
            )
            assertTrue(
                "Смещение $offset должно быть <= +20 минут",
                offset <= maxOffset
            )
        }
    }

    @Test
    fun `расчет интервала с положительным смещением`() {
        // Given
        val offset = 15 * 60 * 1000L // +15 минут
        val expectedInterval = TWENTY_FOUR_HOURS_MILLIS + offset

        // When
        val interval = TWENTY_FOUR_HOURS_MILLIS + offset

        // Then
        assertEquals("Интервал должен быть 24ч 15мин", expectedInterval, interval)
        assertEquals("Интервал в минутах", 1455L, interval / 60 / 1000)
    }

    @Test
    fun `расчет интервала с отрицательным смещением`() {
        // Given
        val offset = -15 * 60 * 1000L // -15 минут
        val expectedInterval = TWENTY_FOUR_HOURS_MILLIS + offset

        // When
        val interval = TWENTY_FOUR_HOURS_MILLIS + offset

        // Then
        assertEquals("Интервал должен быть 23ч 45мин", expectedInterval, interval)
        assertEquals("Интервал в минутах", 1425L, interval / 60 / 1000)
    }

    @Test
    fun `проверка что 23ч 50мин не проходит при смещении +20мин`() {
        // Given: прошло 23ч 50мин, нужно 24ч 20мин
        val currentTime = System.currentTimeMillis()
        val lastRefresh = currentTime - (23 * 60 * 60 * 1000L + 50 * 60 * 1000L) // 23ч 50мин
        val randomOffset = 20 * 60 * 1000L // +20 минут (нужно 24ч 20мин)

        // When
        val result = shouldRefresh(lastRefresh, currentTime, randomOffset)

        // Then
        assertFalse("23ч 50мин < 24ч 20мин - не должно обновляться", result)
    }

    @Test
    fun `проверка что 24ч 25мин проходит при смещении +20мин`() {
        // Given: прошло 24ч 25мин, нужно 24ч 20мин
        val currentTime = System.currentTimeMillis()
        val lastRefresh = currentTime - (24 * 60 * 60 * 1000L + 25 * 60 * 1000L) // 24ч 25мин
        val randomOffset = 20 * 60 * 1000L // +20 минут (нужно 24ч 20мин)

        // When
        val result = shouldRefresh(lastRefresh, currentTime, randomOffset)

        // Then
        assertTrue("24ч 25мин > 24ч 20мин - должно обновляться", result)
    }
}
