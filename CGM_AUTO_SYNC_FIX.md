# Исправление: Автоматическая синхронизация НМГ устройства

## Проблема
При нажатии на кнопку "Синхронизация с устройством" через плюс на главном экране для НМГ устройств ничего не происходило. Синхронизация не запускалась.

## Решение
Добавлена автоматическая синхронизация для НМГ устройств в двух местах:
1. При нажатии кнопки "Синхронизация с устройством" на главном экране
2. При открытии экрана информации об устройстве

## Изменения

### 1. `HomeFlowPm.kt`
**Что изменено:**
- Добавлен `ManageCgmMonitoringUseCase` в конструктор
- Добавлен `Context` в конструктор для запуска Foreground Service
- В Observable для `startSyncAction` добавлена проверка типа устройства
- Для НМГ устройств запускается `manageCgmMonitoringUseCase.startMonitoring(context)` вместо обычной синхронизации
- Отправляется событие `Events.Sync.Glucometer.Success` для обновления UI

**Код до:**
```kotlin
if (isCgmDevice) {
    // Для CGM пропускаем синхронизацию - Service уже работает
    Observable.just(0)
        .doOnSubscribe { bus.event(Events.Sync.Glucometer.NoNewEvents) }
} else {
    syncWithGlucometer(isAuto = false)
}
```

**Код после:**
```kotlin
if (isCgmDevice) {
    // Для CGM запускаем фоновый мониторинг
    Observable.just(0)
        .doOnSubscribe { 
            manageCgmMonitoringUseCase.startMonitoring(context)
            bus.event(Events.Sync.Glucometer.Success)
        }
} else {
    syncWithGlucometer(isAuto = false)
}
```

### 2. `HomeFlowPmVariantA.kt`
**Что изменено:**
- Добавлен `ManageCgmMonitoringUseCase` в конструктор
- Добавлен `Context` в конструктор
- Добавлена проверка типа устройства перед синхронизацией (раньше её не было)
- Для НМГ устройств запускается мониторинг

### 3. `DeviceInfoPm.kt`
**Что изменено:**
- Добавлен `ManageCgmMonitoringUseCase` в конструктор
- Добавлен `Context` в конструктор для запуска Foreground Service
- В методе `handleSuccess()` добавлена автоматическая проверка типа устройства и запуск мониторинга для НМГ
- Добавлен метод `onDestroy()` для остановки мониторинга при выходе с экрана

**Как работает:**
1. При загрузке информации об устройстве проверяется его тип (`data.first.isCgm`)
2. Если это НМГ устройство, автоматически запускается фоновый мониторинг через `manageCgmMonitoringUseCase.startMonitoring(context)`
3. При выходе с экрана мониторинг останавливается через `manageCgmMonitoringUseCase.stopMonitoring(context)`

## Поток работы

### Сценарий 1: Нажатие кнопки "Синхронизация с устройством" на главном экране
```
Пользователь нажимает плюс → "Синхронизация с устройством"
        ↓
startSyncAction срабатывает
        ↓
Загружается список устройств через getDevicesUseCase
        ↓
Проверка: является ли основное устройство НМГ?
        ↓
Если ДА (CGM):
    - Запускается manageCgmMonitoringUseCase.startMonitoring(context)
    - Запускается CgmMonitoringService (Foreground Service)
    - Отправляется Events.Sync.Glucometer.Success
    - Появляется уведомление о мониторинге
    - Данные начинают поступать каждые ~4 секунды
        ↓
Если НЕТ (обычный глюкометр):
    - Запускается syncWithGlucometer(isAuto = false)
    - Обычная BLE синхронизация
```

### Сценарий 2: Открытие экрана информации об устройстве
```
Пользователь открывает экран устройства
        ↓
getDeviceInfoAction загружает данные
        ↓
handleSuccess() получает Glucometer и GlucometerInfo
        ↓
Проверка: if (glucometer.isCgm)
        ↓
Запуск: manageCgmMonitoringUseCase.startMonitoring(context)
        ↓
Запускается CgmMonitoringService (Foreground Service)
        ↓
Сервис получает данные каждые ~4 секунды через BLE Advertising
        ↓
Данные автоматически сохраняются в EventsRepository
        ↓
UI обновляется через GlucoseEventsNotifier
        ↓
При выходе с экрана: onDestroy() → stopMonitoring()
```

## Преимущества
- ✅ Кнопка "Синхронизация с устройством" теперь работает для НМГ
- ✅ Автоматический запуск при открытии экрана НМГ устройства
- ✅ Мониторинг работает в фоне через Foreground Service
- ✅ Автоматически останавливается при выходе с экрана информации
- ✅ Данные сохраняются непрерывно даже при свёрнутом приложении
- ✅ Пользователь получает feedback через событие Success

## Тестирование
1. Добавить НМГ устройство в приложение как основное
2. На главном экране нажать плюс → "Синхронизация с устройством"
3. Проверить, что появилось уведомление о мониторинге
4. Проверить, что данные начали поступать в Events
5. Открыть экран информации об устройстве
6. Проверить, что мониторинг работает
7. Выйти с экрана и проверить, что мониторинг продолжается (запущен с главного экрана)

## Дата исправления
23 января 2026

