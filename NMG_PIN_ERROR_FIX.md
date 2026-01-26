# Исправление ошибки GlucometerPinNotFoundInternaly для НМГ устройств

## Проблема

При нажатии кнопки синхронизации на главном экране для НМГ (CGM) устройства возникала ошибка:
```
com.elta.android.common.errors.GlucometerPinNotFoundInternaly
at com.elta.android.domain.features.devices.interactor.SyncWithGlucometerUseCase.syncWithDevice(SyncWithGlucometerUseCase.kt:85)
```

Также экран синхронизации не завершался при первом подключении НМГ.

## Причина

**Корневая проблема:** Тип устройства (`deviceType`) не сохранялся в базу данных ObjectBox.

1. При добавлении CGM устройства в `AddNewDeviceUseCase` PIN не сохранялся (это правильно - CGM не использует PIN)
2. Но `GlucometerDto` и `GlucometerCachedDto` не имели поля `deviceType`
3. Когда устройство читалось из базы данных, оно получало тип по умолчанию `GLUCOMETER`
4. В результате проверка `isCgmDevice` в `HomeFlowPm` всегда возвращала `false`
5. Приложение пыталось синхронизировать CGM как обычный глюкометр и искало PIN, который не был сохранён

## Решение

Добавлено поле `deviceType` в следующие классы:

### 1. GlucometerCachedDto (ObjectBox Entity)
```kotlin
@Entity
data class GlucometerCachedDto(
    // ...existing fields...
    /** Тип устройства: "GLUCOMETER" или "CGM" */
    val deviceType: String = "GLUCOMETER"
)
```

### 2. GlucometerDto
```kotlin
data class GlucometerDto(
    // ...existing fields...
    /** Тип устройства: "GLUCOMETER" или "CGM" */
    val deviceType: String = "GLUCOMETER"
)
```

### 3. Обновлённые мапперы

- `GlucometerToDtoMapper` - преобразование Domain → DTO с сохранением типа
- `GlucometerToDomainMapper` - преобразование DTO → Domain с восстановлением типа
- `GlucometerToCacheMapper` - преобразование DTO → Cache с сохранением типа
- `GlucometerFromCacheMapper` - преобразование Cache → DTO с восстановлением типа

## Изменённые файлы

1. `data/src/main/java/com/elta/android/data/features/devices/cache/dto/GlucometerCachedDto.kt`
2. `data/src/main/java/com/elta/android/data/features/devices/dto/GlucometerDto.kt`
3. `data/src/main/java/com/elta/android/data/features/devices/mapper/GlucometerToDtoMapper.kt`
4. `data/src/main/java/com/elta/android/data/features/devices/mapper/GlucometerToDomainMapper.kt`
5. `data/src/main/java/com/elta/android/data/features/devices/mapper/GlucometerToCacheMapper.kt`
6. `data/src/main/java/com/elta/android/data/features/devices/mapper/GlucometerFromCacheMapper.kt`

## Важно

После этого исправления:
- Новые устройства будут сохраняться с правильным типом
- Существующие CGM устройства в базе данных будут определяться как `GLUCOMETER` (значение по умолчанию)
- **Рекомендуется:** пользователям с существующими CGM устройствами может потребоваться удалить и заново добавить устройство

## Дата исправления

23 января 2026

