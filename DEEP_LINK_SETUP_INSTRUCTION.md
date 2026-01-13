# 📱 Инструкция для Backend: настройка Deep Links для восстановления пароля

## 🎯 Проблема
Ссылка для восстановления пароля открывается в браузере вместо приложения.

**Ссылка из email:** `https://dev.vdiabete.com/resetpassword?token=...`

---

## ✅ Что нужно сделать на сервере

### 1. Создать файл `assetlinks.json`

Путь на сервере: `https://dev.vdiabete.com/.well-known/assetlinks.json`

**Содержимое файла:**

```json
[
  {
    "relation": ["delegate_permission/common.handle_all_urls"],
    "target": {
      "namespace": "android_app",
      "package_name": "com.elta.android",
      "sha256_cert_fingerprints": [
        "CE:91:11:E9:07:59:BA:63:40:83:FD:46:E5:72:89:31:58:01:BC:0B:00:CB:29:1A:28:89:B3:73:4A:AA:98:32"
      ]
    }
  }
]
```

---

### 2. Требования к размещению файла

✅ **URL должен быть доступен:** `https://dev.vdiabete.com/.well-known/assetlinks.json`

✅ **Content-Type:** `application/json`

✅ **HTTP Status:** `200 OK`

✅ **Без редиректов** (301/302)

✅ **Доступен по HTTPS** (не HTTP)

---

### 3. Как проверить, что файл доступен

Выполните команду в терминале:

```bash
curl https://dev.vdiabete.com/.well-known/assetlinks.json
```

**Ожидаемый результат:** Должен вернуть JSON с данными приложения.

**Или проверьте в браузере:** [https://dev.vdiabete.com/.well-known/assetlinks.json](https://dev.vdiabete.com/.well-known/assetlinks.json)

---

### 4. Проверка через Google инструмент

После размещения файла проверьте через официальный инструмент Google:

🔗 https://developers.google.com/digital-asset-links/tools/generator

**Параметры:**
- **Hosting site domain:** `dev.vdiabete.com`
- **App package name:** `com.elta.android`
- **App package fingerprint (SHA256):** `CE:91:11:E9:07:59:BA:63:40:83:FD:46:E5:72:89:31:58:01:BC:0B:00:CB:29:1A:28:89:B3:73:4A:AA:98:32`

---

## 📋 Какие ссылки должны открываться в приложении

После настройки эти ссылки будут автоматически открывать приложение:

✅ `https://dev.vdiabete.com/resetpassword?token=xxxxx` - **Восстановление пароля**

✅ `https://dev.vdiabete.com/emailconfirmed?token=xxxxx` - **Подтверждение email**

---

## 🔧 Настройка для других окружений

### Stage окружение (`stage2.vdiabete.com`)

Если нужно для stage, создайте файл:
- **URL:** `https://stage2.vdiabete.com/.well-known/assetlinks.json`
- **Содержимое:** То же самое (SHA256 для debug keystore)

### Production окружение (`vdiabete.com`)

Для production нужен **SHA256 отпечаток RELEASE keystore**:

```bash
keytool -list -v -keystore keystore-release.keystore -alias <release_alias> -storepass <password>
```

Затем создайте:
- **URL:** `https://vdiabete.com/.well-known/assetlinks.json`
- **SHA256:** Замените на отпечаток из release keystore

---

## ❓ FAQ

**Q: Почему это нужно?**  
A: Android App Links требуют подтверждения владения доменом через файл `assetlinks.json`. Без него ссылки открываются в браузере.

**Q: Можно ли без этого файла?**  
A: Нет. Это обязательное требование Android для автоматического открытия ссылок в приложении.

**Q: Что если файл недоступен?**  
A: Ссылки будут открываться в браузере (текущее поведение).

---

## 📞 Контакты

Если возникнут вопросы по Android стороне, пишите мне.

После размещения файла на сервере, **мне нужно будет переустановить приложение** для применения изменений.

