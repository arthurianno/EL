# Краткая инструкция: Meal Tag в событиях глюкозы

## Что исправлено
✅ Теперь при синхронизации глюкометра SatelliteVoice состояние "до еды/после еды" корректно передается в события глюкозы.

## Как это работает

### Протокол данных
Response от глюкометра имеет формат:
```
"rd" + дата(12) + температура(3) + глюкоза(3) + meal_tag(1)
```

Позиция 18 (после "rd"): 
- `0` = до еды
- `1` = после еды

### Изменения в коде

#### 1. Модель данных (`GlucometerEvent`)
```kotlin
data class GlucometerEvent(
    // ...существующие поля...
    val mealTag: MealTag? = null  // ← НОВОЕ
)
```

#### 2. Builder (`DefaultGlucometerEventBuilder`)
Добавлен метод извлечения meal tag:
```kotlin
protected open fun extractMealTag(response: String): MealTag? {
    val cleaned = response.replace("rd", "")
    if (cleaned.length > 18) {
        return when (cleaned.substring(18, 19)) {
            "1" -> MealTag.AFTERMEAL
            "0" -> MealTag.BEFOREMEAL
            else -> null
        }
    }
    return null
}
```

#### 3. Mapper (`EventV2FromGlucometerMapper`)
Передача meal tag в событие:
```kotlin
EventV2(
    // ...
    mealTag = mealTag,  // ← Из глюкометра
    // ...
)
```

## Обратная совместимость
- ✅ Старые глюкометры без meal tag: работают как раньше (mealTag = null)
- ✅ Не ломает существующий код
- ✅ Метод можно переопределить для других протоколов

## Настройка для других протоколов
Если SatelliteVoice использует другой формат, создайте подкласс:

```kotlin
class SatelliteVoiceGlucometerEventBuilder @Inject constructor(
    generator: GlucometerEventIdGenerator
) : DefaultGlucometerEventBuilder(generator) {
    
    override fun extractMealTag(response: String): MealTag? {
        // Ваша логика для SatelliteVoice
        // Например, если meal tag на другой позиции или в другом формате
    }
}
```

И зарегистрируйте его в DI-модуле.

## Проверка работы
1. Установите на глюкометре "до еды"
2. Измерьте глюкозу
3. Синхронизируйте
4. Проверьте событие - должно быть "до еды"

То же для "после еды".

