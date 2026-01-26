# Инструкция по отключению/включению НМГ логики

## ⚡ Быстрое отключение через скрипт

Создан скрипт `toggle_nmg.sh` для автоматического управления НМГ логикой:

```bash
# Отключить НМГ
./toggle_nmg.sh disable

# Включить НМГ
./toggle_nmg.sh enable

# Восстановить из резервной копии
./toggle_nmg.sh restore
```

**Преимущества:**
- ✅ Автоматическое комментирование всех блоков
- ✅ Создание резервной копии перед изменениями
- ✅ Быстрое восстановление

## 📝 Ручное отключение

Если предпочитаете делать вручную, все блоки НМГ помечены комментариями:
```kotlin
// ========== НМГ: НАЧАЛО ==========
// ... НМГ код ...
// ========== НМГ: КОНЕЦ ==========
```

### 1. ConnectingViewModel.kt

#### А) Импорты НМГ (строки 14-15):
```kotlin
// ========== НМГ: НАЧАЛО ==========
import com.elta.android.domain.features.devices.interactor.ManageCgmMonitoringUseCase
import com.elta.android.domain.features.devices.interactor.SyncWithCgmUseCase
// ========== НМГ: КОНЕЦ ==========
```

#### Б) Параметры конструктора (строки 73-74):
```kotlin
class ConnectingViewModel @Inject constructor(
    private val findGlucometers: FindGlucometersUseCase,
    private val connectDevice: AddNewDeviceUseCase,
    private val syncWithGlucometer: SyncWithGlucometerUseCase,
    // ========== НМГ: НАЧАЛО ==========
    private val syncWithCgm: SyncWithCgmUseCase,
    private val manageCgmMonitoring: ManageCgmMonitoringUseCase,
    // ========== НМГ: КОНЕЦ ==========
    private val getLocationNeededUseCase: GetLocationNeededUseCase,
    ...
)
```

#### В) Логика синхронизации с НМГ (строки ~360-430):
```kotlin
private fun syncDevice() {
    deviceConnected = true
    syncJob?.cancel()
    connectJob?.cancel()

    val connectedDevice = state.value.connectDevice

    // ========== НМГ: НАЧАЛО ==========
    // Если устройство - НМГ датчик, используем специальную логику синхронизации
    if (connectedDevice?.deviceType == DeviceType.CGM) {
        crashlyticsReport.log("CGM: Запуск Foreground Service для фонового мониторинга")
        // ... весь блок кода с НМГ ...
    } else {
    // ========== НМГ: КОНЕЦ ==========
        // Для обычного глюкометра используем стандартную синхронизацию
        syncJob = launch {
            syncWithGlucometer.execute(SyncWithGlucometerUseCase.Params(connectedDevice))
                .doOnComplete {
                    reduceState { state.value.copy(stageType = ConnectingStageType.Complete) }
                }
                .asFlow()
                .catch { handleSyncError(it) }
                .collect()
        }
    // ========== НМГ: НАЧАЛО ==========
    }
    // ========== НМГ: КОНЕЦ ==========
}
```

#### Г) Логика выхода с НМГ (строки ~460-475):
```kotlin
private fun exitFromScreen() {
    connectJob?.cancel()

    // ========== НМГ: НАЧАЛО ==========
    // Для CGM НЕ останавливаем мониторинг
    val isCgmDevice = state.value.connectDevice?.deviceType == DeviceType.CGM

    if (isCgmDevice) {
        syncJob?.cancel()
        crashlyticsReport.log("CGM: Закрытие экрана, мониторинг продолжает работать в фоне")
    } else {
    // ========== НМГ: КОНЕЦ ==========
        syncJob?.cancel()
    // ========== НМГ: НАЧАЛО ==========
    }
    // ========== НМГ: КОНЕЦ ==========

    router.backTo(Screens.ScannerDmcScreen(state.value.isOnBoarding))
}
```

#### Д) completeConnect() метод (строки ~231-250):
```kotlin
private fun completeConnect() {
    launch {
        try {
            updateUserInfo.execute(UpdateUserInfoUseCase.Params(UserInfo(isFirstSync = true)))
                .await()

            bus.event(Events.DeviceChanged)
            bus.event(Events.EventsChanged(true))

            // ========== НМГ: НАЧАЛО ==========
            // Для CGM НЕ останавливаем мониторинг - он продолжит работать в фоне
            // syncJob будет жить в scope ViewModel'и
            // ========== НМГ: КОНЕЦ ==========

            if (state.value.isOnBoarding) router.newRootScreen(Screens.HomeFlow)
            else router.backTo(Screens.Devices)

        } catch (e: Exception) {
            handleError(e)
        }
    }
}
```

### 2. ScannerDmcFragment.kt

Нужно закомментировать кнопку "НМГ датчик (без QR-кода)" в InfoSheet и плавающую кнопку.

### 3. CgmModule.kt (DI модуль)

Можно закомментировать весь модуль, но это может вызвать ошибки компиляции.

## Быстрое отключение

Чтобы быстро отключить НМГ, достаточно:

1. **Закомментировать параметры в конструкторе ConnectingViewModel**
2. **Удалить if-блок с DeviceType.CGM** в методе `syncDevice()`, оставив только else-ветку
3. **Удалить проверку isCgmDevice** в методе `exitFromScreen()`

## Восстановление

Чтобы восстановить НМГ логику:
1. Найти все комментарии `// ========== НМГ: НАЧАЛО/КОНЕЦ ==========`
2. Раскомментировать код между ними

