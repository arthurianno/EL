---
## Инструкция для Backend
### Шаг 1: Получить SHA256 отпечаток ключа
Результат:
```
SHA256: === RELEASE KEY ===
         SHA256: 5F:D0:2C:94:55:E1:17:32:0A:19:5B:D0:E5:88:B2:A2:37:A5:1A:E8:90:E6:F0:DF:CD:85:68:62:18:17:64:CB
SHA256: === DEBUG KEY ===
         SHA256: 9D:26:FA:09:B1:66:73:F3:B5:84:39:B7:00:C9:BD:A6:41:F6:B4:F1:58:EA:CF:83:E9:18:94:52:B8:DA:35:42
```
### Шаг 2: Создать JSON файл
**Содержимое файла assetlinks.json:**
```json
[{
  "relation": ["delegate_permission/common.handle_all_urls"],
  "target": {
    "namespace": "android_app",
    "package_name": "com.elta.android",
    "sha256_cert_fingerprints": [
               SHA256
    ]
  }
}]
### Шаг 3: Разместить файл на сервере

### ⚠️ КРИТИЧЕСКИ ВАЖНО: Публичный доступ

**Файл `assetlinks.json` ОБЯЗАТЕЛЬНО должен быть доступен БЕЗ авторизации!**

❌ **Не работает:**
```bash
# URL требует авторизацию
curl https://vdiabete.com/.well-known/assetlinks.json
→ 401 Unauthorized / 403 Forbidden / Редирект на логин
```

✅ **Работает:**
```bash
# URL доступен публично
curl https://vdiabete.com/.well-known/assetlinks.json
→ 200 OK + JSON содержимое
```

**Почему это важно:**
- Android проверяет файл при установке приложения
- Проверка происходит БЕЗ cookies, БЕЗ заголовков авторизации
- Если файл недоступен → deep links НЕ РАБОТАЮТ

**Как это исправить на backend:**

Для **nginx**:
```nginx
# Публичный доступ к assetlinks.json БЕЗ авторизации
location /.well-known/assetlinks.json {
    alias /path/to/assetlinks.json;
    default_type application/json;
    add_header Access-Control-Allow-Origin *;
    # НЕТ auth_basic или других проверок!
}

# Остальные пути с авторизацией
location / {
    auth_basic "Restricted";
    ...
}
```

Для **Laravel**:
```php
Route::get('/.well-known/assetlinks.json', function () {
    // ...
})->withoutMiddleware(['auth']); // БЕЗ авторизации!
```

Для **Express**:
```javascript
// Публичный endpoint БЕЗ authMiddleware
app.get('/.well-known/assetlinks.json', (req, res) => {
    // ...
});

// Остальные endpoints с авторизацией
app.use(authMiddleware);
```

---

Файл должен быть доступен по следующим URL:
**Production:**
```
https://vdiabete.com/.well-known/assetlinks.json
```
**Debug:**
```
https://dev.vdiabete.com/.well-known/assetlinks.json
```
### Способы размещения:
#### Вариант A: Статический файл (рекомендуется)
Поместить файл в директорию статики:
```
/public/.well-known/assetlinks.json
или
/static/.well-known/assetlinks.json
```
#### Вариант B: Настройка nginx
```nginx
location /.well-known/assetlinks.json {
    alias /path/to/assetlinks.json;
    default_type application/json;
    add_header Access-Control-Allow-Origin *;
}
```
#### Вариант C: Роут в приложении
**Laravel пример:**
```php
Route::get('/.well-known/assetlinks.json', function () {
    return response()->json([[
        "relation" => ["delegate_permission/common.handle_all_urls"],
        "target" => [
            "namespace" => "android_app",
            "package_name" => "com.elta.android",
            "sha256_cert_fingerprints" => ["YOUR_SHA256_HERE"]
        ]
    ]]);
});
```
**Node.js/Express пример:**
```javascript
app.get('/.well-known/assetlinks.json', (req, res) => {
    res.json([{
        relation: ["delegate_permission/common.handle_all_urls"],
        target: {
            namespace: "android_app",
            package_name: "com.elta.android",
            sha256_cert_fingerprints: ["YOUR_SHA256_HERE"]
        }
    }]);
});
```
### Шаг 4: Проверить доступность
После размещения проверьте в браузере:
```
https://vdiabete.com/.well-known/assetlinks.json
```
**Должно вернуться:**
- HTTP статус: `200 OK`
- Content-Type: `application/json`
- JSON с вашим приложением
### Требования:
✅ Файл доступен по HTTPS (не HTTP!)
✅ Путь точно `/.well-known/assetlinks.json` (регистр важен!)
✅ Content-Type: `application/json`
✅ Публичный доступ (без авторизации)
✅ SHA256 точно совпадает с ключом приложения
---
## FAQ
### Q: Можно ли обойтись без файла assetlinks.json?
**A:** Нет, для HTTPS deep links файл обязателен. Это требование Android для безопасности.
**Альтернатива:** Использовать кастомную схему `elta://`, но:
- ❌ Gmail покажет предупреждение "Открыть в приложении?"
- ❌ Выглядит непрофессионально
- ❌ Пользователь может отказаться
- ✅ Не требует assetlinks.json
### Q: Кто проверяет файл assetlinks.json?
**A:** Android OS  автоматически при установке приложения.
Процесс:
1. Пользователь устанавливает приложение
2. Android читает AndroidManifest.xml
3. Видит `android:autoVerify="true"`
4. Делает HTTP запрос на `https://vdiabete.com/.well-known/assetlinks.json`
5. Проверяет SHA256
6. Сохраняет результат: verified ✅ или failed ❌
### Q: Как часто происходит проверка?
**A:** 
- При установке приложения ✅
- При обновлении (если изменился ключ подписи) ✅
- Больше не проверяется (результат кешируется)
### Q: Что если файл появится после установки приложения?
**A:** Нужно переустановить приложение или принудительно запустить проверку:
```bash
adb shell pm verify-app-links --re-verify com.elta.android
```
### Q: Работает ли это на iOS?
**A:** На iOS похожая система называется Universal Links. Нужен файл:
```
https://vdiabete.com/apple-app-site-association
```
Это другой формат, нужна отдельная настройка.
### Q: Нужно ли обновлять файл при каждом релизе?
**A:** Нет! Файл нужно обновлять только если:
- Изменился ключ подписи приложения
- Изменился package name
- Добавляется новое приложение для того же домена
### Q: Можно ли добавить несколько приложений в один файл?
**A:** Да! Просто добавьте несколько объектов в массив:
```json
[
  {
    "relation": ["delegate_permission/common.handle_all_urls"],
    "target": {
      "namespace": "android_app",
      "package_name": "com.elta.android",
      "sha256_cert_fingerprints": ["SHA256_MAIN_APP"]
    }
  },
  {
    "relation": ["delegate_permission/common.handle_all_urls"],
    "target": {
      "namespace": "android_app",
      "package_name": "com.elta.android.debug",
      "sha256_cert_fingerprints": ["SHA256_DEBUG_APP"]
    }
  }
]
```
### Q: Как проверить, что всё работает правильно?
**A:** Используйте официальный инструмент Google:
https://developers.google.com/digital-asset-links/tools/generator
Введите:
- Site domain: `vdiabete.com`
- App package name: `com.elta.android`
- SHA256 fingerprint: ваш SHA256
Инструмент проверит файл и покажет результат.
---
## Полезные ссылки
- [Official Android Documentation](https://developer.android.com/training/app-links/verify-android-applinks)
- [Digital Asset Links Tool](https://developers.google.com/digital-asset-links/tools/generator)
- [Testing App Links](https://developer.android.com/training/app-links/verify-android-applinks#testing)
---
## Чек-лист для запуска
### Backend:
- [ ] Получен SHA256 от Android-разработчика
- [ ] Создан файл assetlinks.json с правильным содержимым
- [ ] Файл доступен по `https://vdiabete.com/.well-known/assetlinks.json`
- [ ] Файл доступен по `https://dev.vdiabete.com/.well-known/assetlinks.json`
- [ ] HTTP статус 200 OK
- [ ] Content-Type: application/json
### Android:
- [ ] Получен и отправлен SHA256 backend-команде
- [ ] Дождались создания файла на сервере
- [ ] Переустановили приложение
- [ ] Проверили статус верификации (должен быть "verified")
- [ ] Протестировали через ADB - работает ✅
- [ ] Протестировали из Gmail - работает ✅
---
## Контакты и поддержка
При возникновении проблем:
1. Проверьте все пункты чек-листа
2. Используйте команды из раздела "Отладка проблем"
3. Проверьте файл через Google инструмент
**Дата создания документации:** 24 декабря 2024
