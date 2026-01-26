# Исправление: Meal Tag при синхронизации с глюкометром SatelliteVoice

## Проблема
При синхронизации глюкометра SatelliteVoice в событии "Глюкоза" не проставлялось состояние (до еды/после еды), заданное в глюкометре.

## Причина
В текущей реализации `DefaultGlucometerEventBuilder` извлекались только базовые данные из response строки глюкометра:
- Дата измерения (12 символов)
- Температура (3 символа)
- Значение глюкозы (3 символа)

Информация о meal tag (до еды/после еды), которая может находиться в конце response строки, не извлекалась и не передавалась в событие.

## Решение

### 1. Расширение модели данных
Добавлено поле `mealTag: MealTag?` в:
- `GlucometerEvent` (domain/features/devices/model)
- `GlucometerEventDto` (data/features/devices/dto)

### 2. Извлечение meal tag из response
В `DefaultGlucometerEventBuilder` добавлен метод `extractMealTag()`:
```kotlin
protected open fun extractMealTag(response: String): MealTag? {
    return try {
        val cleaned = response.replace("rd", "")
        // После даты(12) + температуры(3) + значения(3) = 18 символов
        if (cleaned.length > 18) {
            val mealTagChar = cleaned.substring(18, 19)
            when (mealTagChar) {
                "1" -> MealTag.AFTERMEAL
                "0" -> MealTag.BEFOREMEAL
                else -> null
            }
        } else {
            null
        }
    } catch (ex: Exception) {
        Timber.e(ex, "Error extracting meal tag from response: $response")
        null
    }
}
```

Метод `buildFrom()` обновлен для вызова `extractMealTag()` и установки значения в `GlucometerEvent`.

### 3. Передача meal tag в событие
В `EventV2FromGlucometerMapper` обновлено маппинг для передачи meal tag:
```kotlin
mealTag = mealTag, // Передаем meal tag из глюкометра
```

## Протокол глюкометра
Формат response строки:
```
"rd" + дата(12 символов) + температура(3) + значение(3) + [дополнительные данные]
```

Meal tag находится на позиции 18 (после "rd"):
- `"0"` или отсутствует = до еды (BEFOREMEAL)
- `"1"` = после еды (AFTERMEAL)

## Архитектурные принципы
Решение следует существующей архитектуре проекта:
- ✅ Изменения в domain слое (модель данных)
- ✅ Изменения в data слое (DTO, builder, mapper)
- ✅ Метод `extractMealTag()` объявлен как `protected open` для возможности переопределения в подклассах
- ✅ Обработка ошибок с логированием через Timber
- ✅ Значение по умолчанию `null` - не ломает существующий код
- ✅ Обратная совместимость с глюкометрами, не передающими meal tag

## Тестирование
Для проверки исправления:
1. Синхронизировать глюкометр SatelliteVoice с установленным состоянием "до еды"
2. Проверить, что в событии глюкозы отображается состояние "до еды"
3. Синхронизировать с состоянием "после еды"
4. Проверить, что в событии отображается "после еды"

## Измененные файлы
1. `domain/features/devices/model/GlucometerEvent.kt`
2. `data/features/devices/dto/GlucometerEventDto.kt`
3. `data/features/devices/glucometer/builder/DefaultGlucometerEventBuilder.kt`
4. `data/features/diary/events/mapper/v2/EventV2FromGlucometerMapper.kt`

## Дополнительные замечания
- Если протокол глюкометра использует другую позицию или формат для meal tag, метод `extractMealTag()` можно легко переопределить в подклассе
- Решение не влияет на синхронизацию других типов глюкометров
- При отсутствии meal tag в response событие создается с `mealTag = null` (как и раньше)

